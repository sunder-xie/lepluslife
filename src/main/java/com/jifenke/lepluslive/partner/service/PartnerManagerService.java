package com.jifenke.lepluslive.partner.service;

import com.jifenke.lepluslive.lejiauser.domain.entities.LeJiaUser;
import com.jifenke.lepluslive.lejiauser.repository.LeJiaUserRepository;
import com.jifenke.lepluslive.merchant.domain.entities.Merchant;
import com.jifenke.lepluslive.merchant.repository.MerchantRepository;
import com.jifenke.lepluslive.partner.controller.dto.PartnerManagerDto;
import com.jifenke.lepluslive.partner.domain.criteria.PartnerManagerCriteria;
import com.jifenke.lepluslive.partner.domain.entities.Partner;
import com.jifenke.lepluslive.partner.domain.entities.PartnerManager;
import com.jifenke.lepluslive.partner.domain.entities.PartnerManagerWallet;
import com.jifenke.lepluslive.partner.repository.*;
import com.jifenke.lepluslive.weixin.domain.entities.WeiXinUser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xf on 17-3-17.
 */
@Service
@Transactional(readOnly = true)
public class PartnerManagerService {
    @Inject
    private PartnerManagerRepository partnerManagerRepository;

    @Inject
    private PartnerManagerWalletRepository partnerManagerWalletRepository;

    @Inject
    private LeJiaUserRepository leJiaUserRepository;

    @Inject
    private MerchantRepository merchantRepository;

    @Inject
    private PartnerManagerWalletLogRepository partnerManagerWalletLogRepository;

    @Inject
    private PartnerRepository partnerRepository;

    @Inject
    private PartnerWalletLogRepository partnerWalletLogRepository;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PartnerManager findByPartnerAccountId(Long accountId) {
        return partnerManagerRepository.findByPartnerId(accountId);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PartnerManagerWallet findWalletByPartnerManager(PartnerManager partnerManager) {
        return partnerManagerWalletRepository.findByPartnerManager(partnerManager);
    }

    /**
     * 城市合伙人每日佣金收入
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Long findDailyCommissionByPartnerManager(Long partnerManagerId) {
        return partnerManagerWalletLogRepository.findDailyCommissionByPartnerManager(partnerManagerId);
    }


    /**
     * 根据 Sid 查询PartnerManager
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public PartnerManager findByPartnerManagerSid(String partnerManagerSid) {
        return partnerManagerRepository.findByPartnerManagerSid(partnerManagerSid);
    }

    @Transactional(readOnly = true,propagation = Propagation.REQUIRED)
    public Optional<Partner> findByWeiXinUser(WeiXinUser weiXinUser) {
        return partnerManagerRepository.findByWeiXinUser(weiXinUser);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Boolean bindWeiXinUser(PartnerManager partnerManager, WeiXinUser weiXinUser) {
        if (partnerManager != null && weiXinUser != null) {
            LeJiaUser leJiaUser = weiXinUser.getLeJiaUser();
            boolean flag = true;
            if (leJiaUser.getBindPartnerManager()==null||!leJiaUser.getBindPartner().getId().equals(partnerManager.getId())) { //未绑上
                    leJiaUser.setBindPartnerManager(partnerManager);
                    leJiaUserRepository.save(leJiaUser);
                    partnerManager.setWeiXinUser(weiXinUser);
                    partnerManagerRepository.save(partnerManager);
            } else {
                partnerManager.setWeiXinUser(weiXinUser);
                partnerManagerRepository.save(partnerManager);
            }
            if (flag) {
                new Thread(() -> {
                    String
                            getUrl =
                            "http://www.lepluspay.com/api/partnerManager/bind_wx_user/" + partnerManager.getPartnerManagerSid();
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet(getUrl);
                    httpGet.addHeader("Content-Type", "text/html;charset=UTF-8");
                    CloseableHttpResponse response = null;
                    try {
                        response = httpclient.execute(httpGet);
                        HttpEntity entity = response.getEntity();
                        EntityUtils.consume(entity);
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            return flag;
        }
        return false;
    }
}
