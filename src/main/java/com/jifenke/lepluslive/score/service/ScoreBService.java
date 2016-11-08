package com.jifenke.lepluslive.score.service;

import com.jifenke.lepluslive.lejiauser.domain.entities.LeJiaUser;
import com.jifenke.lepluslive.score.domain.entities.ScoreB;
import com.jifenke.lepluslive.score.domain.entities.ScoreBDetail;
import com.jifenke.lepluslive.score.repository.ScoreBDetailRepository;
import com.jifenke.lepluslive.score.repository.ScoreBRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by wcg on 16/3/18.
 */
@Service
@Transactional(readOnly = true)
public class ScoreBService {

  @Inject
  private ScoreBRepository scoreBRepository;

  @Inject
  private ScoreBDetailRepository scoreBDetailRepository;

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public ScoreB findScoreBByWeiXinUser(LeJiaUser leJiaUser) {
    return scoreBRepository.findByLeJiaUser(leJiaUser);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public List<ScoreBDetail> findAllScoreBDetail(ScoreB scoreB) {
    return scoreBDetailRepository.findAllByScoreBOrderByDateCreatedDesc(scoreB);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void paySuccess(LeJiaUser leJiaUser, Long totalScore, Integer origin, String operate,
                         String orderSid) {
    ScoreB scoreB = findScoreBByWeiXinUser(leJiaUser);

    if (scoreB.getScore() - totalScore >= 0) {
      scoreB.setScore(scoreB.getScore() - totalScore);
      ScoreBDetail scoreBDetail = new ScoreBDetail();
      scoreBDetail.setOperate(operate);
      scoreBDetail.setOrigin(origin);
      scoreBDetail.setOrderSid(orderSid);
      scoreBDetail.setScoreB(scoreB);
      scoreBDetail.setNumber(-totalScore);
      scoreBDetailRepository.save(scoreBDetail);
      scoreBRepository.save(scoreB);
    } else {
      throw new RuntimeException("积分不足");
    }

  }

  /**
   * 根据scoreB查询积分明细列表
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public List<ScoreBDetail> findAllScoreBDetailByScoreB(ScoreB scoreB) {
    return scoreBDetailRepository.findAllByScoreBOrderByDateCreatedDesc(scoreB);
  }
}
