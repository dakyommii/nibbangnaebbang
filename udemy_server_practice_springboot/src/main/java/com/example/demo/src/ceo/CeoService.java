package com.example.demo.src.ceo;


import com.example.demo.config.BaseException;

import com.example.demo.src.ceo.model.PatchCeoReq;
import com.example.demo.src.ceo.model.PostCeoReq;
import com.example.demo.src.ceo.model.PostCeoRes;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class CeoService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CeoDao ceoDao;
    private final CeoProvider ceoProvider;
    private final JwtService jwtService;


    @Autowired
    public CeoService(CeoDao ceoDao, CeoProvider ceoProvider, JwtService jwtService) {
        this.ceoDao = ceoDao;
        this.ceoProvider = ceoProvider;
        this.jwtService = jwtService;

    }


    /* 회원가입 */
    public PostCeoRes createCeo(PostCeoReq postCeoReq) throws BaseException {
        // 사업자등록번호 중복 확인
        if(ceoProvider.checkStoreNum(postCeoReq.getStoreNum()) ==1){
            throw new BaseException(POST_CEO_EXISTS_STORE_NUM);
        }

        // 아이디 중복 확인
        if(ceoProvider.checkCeoId(postCeoReq.getCeoId()) ==1){
            throw new BaseException(POST_CEO_EXISTS_CEO_ID);
        }

        // 핸드폰 번호 중복 확인
        if(ceoProvider.checkStoreNum(postCeoReq.getCeoPhone()) ==1){
            throw new BaseException(POST_CEO_EXISTS_CEO_PHONE);
        }

        String pwd;
        try{
            //비밀번호 암호화
            pwd = new SHA256().encrypt(postCeoReq.getCeoPwd());
            postCeoReq.setCeoPwd(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try{
            int ceoIdx = ceoDao.createCeo(postCeoReq);

            //jwt 발급.
            String jwt = jwtService.createJwt(ceoIdx);
            return new PostCeoRes(jwt,ceoIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /* 로그인 */
    public void loginCeo(GetCeoReq getCeoReq) throws BaseException {
        try {
            String password;

            password = new SHA256().encrypt(getCeoReq.getCeoPwd());

            String check = ceoDao.loginCeo(getCeoReq.getCeoId());

            if (!password.equals(check))
                throw new BaseException(CEO_ERROR_CEO_PWD);
        } catch (Exception exception)
        throw new BaseException(DATABASE_ERROR);
    }

    /* 비밀번호 변경 */
    public PatchCeoPwdRes modifyCeoPwd(PatchCeoPwdReq patchCeoPwdReq) throws BaseException {
        //아이디 존재여부 check
        if(ceoProvider.checkCeoId(patchCeoPwdReq.getCeoId()) ==0){
            throw new BaseException(CEO_EMPTY_CEO_ID);
        }

        String pwd;
        try{
            //비밀번호 암호화
            pwd = new SHA256().encrypt(patchCeoPwdReq.getModifyCeoPwd());
            patchCeoPwdReq.setModifyCeoPwd(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try{
            int ceoIdx = ceoDao.modifyCeoPwd(patchCeoPwdReq);

            //jwt 발급.
            String jwt = jwtService.createJwt(ceoIdx);
            return new patchCeoPwdRes(jwt,ceoIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

}