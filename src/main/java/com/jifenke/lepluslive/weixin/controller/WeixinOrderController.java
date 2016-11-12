package com.jifenke.lepluslive.weixin.controller;

import com.jifenke.lepluslive.global.util.LejiaResult;
import com.jifenke.lepluslive.global.util.MvUtil;
import com.jifenke.lepluslive.order.domain.entities.OnLineOrder;
import com.jifenke.lepluslive.score.domain.entities.ScoreA;
import com.jifenke.lepluslive.score.domain.entities.ScoreB;
import com.jifenke.lepluslive.Address.domain.entities.Address;
import com.jifenke.lepluslive.weixin.domain.entities.WeiXinUser;
import com.jifenke.lepluslive.Address.service.AddressService;
import com.jifenke.lepluslive.order.service.OrderService;
import com.jifenke.lepluslive.score.service.ScoreAService;
import com.jifenke.lepluslive.score.service.ScoreBService;
import com.jifenke.lepluslive.weixin.service.WeiXinPayService;
import com.jifenke.lepluslive.weixin.service.WeiXinService;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by wcg on 16/3/20.
 */
@Controller
@RequestMapping("/weixin")
public class WeixinOrderController {

  @Inject
  private OrderService orderService;

  @Inject
  private WeiXinService weiXinService;

  @Inject
  private ScoreBService scoreBService;

  @Inject
  private ScoreAService scoreAService;

  @Inject
  private AddressService addressService;

  @Inject
  private WeiXinPayService weiXinPayService;

  //跳转到支付页面
  @RequestMapping("/pay/confirm")
  public ModelAndView goPayPage(@RequestParam Long orderId, @RequestParam Long addressId,
                                Model model, HttpServletRequest request) {
    model.addAttribute("orderSid", orderService.orderConfirm(orderId, addressId));

    return MvUtil.go("/weixin/pay");
  }

  /**
   * 个人订单详情页
   */
  @RequestMapping("/orderDetail")
  public ModelAndView goOrderDetailPage() {
    return MvUtil.go("/weixin/orderDetail");
  }

  @RequestMapping("/scoreDetail")
  public ModelAndView goScoreDetailPage(HttpServletRequest request, Model model,
                                        @RequestParam Integer type) {
    WeiXinUser weiXinUser = weiXinService.getCurrentWeiXinUser(request);
    ScoreA scoreA = scoreAService.findScoreAByLeJiaUser(weiXinUser.getLeJiaUser());
    ScoreB scoreB = scoreBService.findScoreBByWeiXinUser(weiXinUser.getLeJiaUser());
    model.addAttribute("scoreADetails", scoreAService.findAllScoreADetailByScoreA(scoreA));
    model.addAttribute("scoreBDetails", scoreBService.findAllScoreBDetailByScoreB(scoreB));
    model.addAttribute("openId", weiXinUser.getOpenId());
    model.addAttribute("type", type);
    return MvUtil.go("/weixin/scoreDetail");

  }


  /**
   * 订单取消
   */
  @RequestMapping(value = "/order/orderCancle", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public
  @ResponseBody
  LejiaResult orderCancle(Long orderId) {
    orderService.orderCancle(orderId);
    return LejiaResult.ok();
  }

  @RequestMapping(value = "/order/orderConfirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public
  @ResponseBody
  LejiaResult orderConfim(Long orderId) {
    orderService.confrimOrder(orderId);
    return LejiaResult.ok();
  }

  /**
   * 立刻支付按钮跳转到支付页面  16/09/23
   */
  @RequestMapping(value = "/order/{orderId}", method = RequestMethod.GET)
  public ModelAndView orderPay(@PathVariable Long orderId, HttpServletRequest request,
                               @RequestParam(required = false) Boolean flag,
                               Model model) {
    WeiXinUser weiXinUser = weiXinService.getCurrentWeiXinUser(request);
    model.addAttribute("scoreB",
                       scoreBService.findScoreBByWeiXinUser(weiXinUser.getLeJiaUser()).getScore());
    model.addAttribute("order", orderService.findOnLineOrderById(orderId));
    model.addAttribute("address",
                       addressService.findAddressByLeJiaUserAndState(weiXinUser.getLeJiaUser()));
    model.addAttribute("wxConfig", weiXinPayService.getWeiXinPayConfig(request));
    return MvUtil.go("/order/confirmOrder");
  }

  /**
   * 从支付页面跳转到地址修改页面
   */
  @RequestMapping("/order/addressEdit/{orderId}")
  public ModelAndView goAddressEditPage(@PathVariable Long orderId, Model model,
                                        HttpServletRequest request) {
    Address
        address =
        addressService.findAddressByLeJiaUserAndState(
            weiXinService.getCurrentWeiXinUser(request).getLeJiaUser());
    model.addAttribute("address", address);
    model.addAttribute("orderId", orderId);
    return MvUtil.go("/weixin/address");
  }

  /**
   * 修改地址并跳转到支付页面
   */
  @RequestMapping(value = "/order/{orderId}", method = RequestMethod.POST)
  public ModelAndView editAddress(Address address, @PathVariable Long orderId, Model model,
                                  HttpServletRequest request) {
    OnLineOrder onLineOrder = orderService.findOnLineOrderById(orderId);
    WeiXinUser weiXinUser = weiXinService.getCurrentWeiXinUser(request);
    addressService.editAddress(address, weiXinUser, onLineOrder);
    model.addAttribute("order", onLineOrder);
    model.addAttribute("wxConfig", weiXinPayService.getWeiXinPayConfig(request));
    model.addAttribute("canUseScore",
                       scoreBService.findScoreBByWeiXinUser(weiXinUser.getLeJiaUser())
                           .getScore()); //用户可用积分
    return MvUtil.go("/order/confirmOrder");
  }

  /**
   * 获取用户所有的订单
   */
  @RequestMapping(value = "/order/orderList")
  public
  @ResponseBody
  LejiaResult getCurrentUserAllOrder(HttpServletRequest request) {
    List<OnLineOrder>
        onLineOrders =
        orderService
            .getCurrentUserOrders(weiXinService.getCurrentWeiXinUser(request).getLeJiaUser(), 5);
    return LejiaResult.ok(onLineOrders);
  }


}
