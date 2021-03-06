package com.jifenke.lepluslive.partner.service;

import com.jifenke.lepluslive.partner.domain.entities.*;
import com.jifenke.lepluslive.partner.repository.PartnerManagerWalletOnlineLogRepository;
import com.jifenke.lepluslive.partner.repository.PartnerManagerWalletOnlineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by xf on 2017/4/28.
 */
@Service
@Transactional(readOnly = true)
public class PartnerManagerWalletOnlineService {
    @Inject
    private PartnerManagerWalletOnlineRepository walletOnlineRepository;
    @Inject
    private PartnerManagerWalletOnlineLogRepository logRepository;
    /**
     *  获取城市合伙人线上钱包
     */
    @Transactional(readOnly = true,propagation = Propagation.REQUIRED)
    public PartnerManagerWalletOnline findByPartnerManager(PartnerManager partnerManager) {
        return walletOnlineRepository.findByPartnerManager(partnerManager);
    }

    /**
     *  创建城市合伙人线上钱包
     */
    @Transactional(readOnly = true,propagation = Propagation.REQUIRED)
    public void savePartnerManagerWalletOnline(PartnerManagerWalletOnline partnerManagerWalletOnline) {
        walletOnlineRepository.save(partnerManagerWalletOnline);
    }

    /**
     * 向城市合伙人分润    2017/04/28
     *
     * @param shareMoney    分润金额
     * @param partnerManager 城市合伙人
     * @param partnerManagerWallet  城市合伙人线上钱包
     * @param orderSid      线上订单号
     * @param type          1代表app线上订单分润  2代表公众号线上订单分润
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void shareToPartnerManager(Long shareMoney, PartnerManager partnerManager, PartnerManagerWalletOnline partnerManagerWallet,
                               String orderSid, Long type) {
        PartnerManagerWalletOnlineLog log = new PartnerManagerWalletOnlineLog();
        Long availableBalance = partnerManagerWallet.getAvailableBalance();
        log.setBeforeChangeMoney(availableBalance);
        long afterShareMoney = availableBalance + shareMoney;
        log.setAfterChangeMoney(afterShareMoney);
        log.setChangeMoney(shareMoney);

        log.setPartnerManagerId(partnerManager.getId());

        log.setOrderSid(orderSid);

        log.setType(type);

        partnerManagerWallet.setTotalMoney(partnerManagerWallet.getTotalMoney() + shareMoney);

        partnerManagerWallet.setAvailableBalance(afterShareMoney);

        logRepository.save(log);

        walletOnlineRepository.save(partnerManagerWallet);

    }
}
