package com.jifenke.lepluslive.Address.service;

import com.jifenke.lepluslive.Address.domain.entities.Address;
import com.jifenke.lepluslive.Address.repository.AddressRepository;
import com.jifenke.lepluslive.global.util.LejiaResult;
import com.jifenke.lepluslive.lejiauser.domain.entities.LeJiaUser;
import com.jifenke.lepluslive.order.domain.entities.OnLineOrder;
import com.jifenke.lepluslive.order.service.OrderService;
import com.jifenke.lepluslive.weixin.domain.entities.WeiXinUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by wcg on 16/3/21.
 */
@Service
@Transactional(readOnly = true)
public class AddressService {

  @Inject
  private AddressRepository addressRepository;

  @Inject
  private OrderService orderService;

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Address findOneAddress(Long id) {
    return addressRepository.findOne(id);
  }

  public Address findAddressByLeJiaUserAndState(LeJiaUser leJiaUser) {
    Address address = addressRepository.findByLeJiaUserAndState(leJiaUser, 1);
    if (address != null) {
      return address;
    } else {
      List<Address> addressList = addressRepository.findByLeJiaUserAndStateNot(leJiaUser, 2);
      if(addressList.size()>0){
        return addressList.get(0);
      }
      return null;
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public Address editAddress(Address address, WeiXinUser weiXinUser, OnLineOrder onLineOrder) {
    address.setLeJiaUser(weiXinUser.getLeJiaUser());
    addressRepository.save(address);
    onLineOrder.setAddress(address);
    orderService.editAddress(onLineOrder);
    return address;

  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public List<Address> findAllAddressByLeJiaUser(LeJiaUser leJiaUser) {
    return addressRepository.findAllAddressByLeJiaUserAndStateNot(leJiaUser, 2);
  }

  /**
   * app端 新增或修改收货地址
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public Address editAddress(LeJiaUser leJiaUser, String name, String location, String phoneNumber,
                             String province, String city, String county, Long id) {
    Address address = null;
    if (id != null) {
      address = addressRepository.findOne(id);
    } else {
      address = new Address();
      address.setLeJiaUser(leJiaUser);
    }
    if (address != null) {
      address.setName(name);
      address.setCity(city);
      address.setCounty(county);
      address.setLocation(location);
      address.setPhoneNumber(phoneNumber);
      address.setProvince(province);
    } else {
      return null;
    }
    addressRepository.save(address);
    return address;
  }

  /**
   * 修改默认收货地址
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public LejiaResult changeState(Long oldId, Long newId) {
    Address oldAddress = null;
    Address newAddress = null;
    if (oldId != null) {
      oldAddress = addressRepository.findOne(oldId);
      if (oldAddress == null) {
        return LejiaResult.build(301, "未找到收货地址记录");
      }
      oldAddress.setState(0);
      addressRepository.save(oldAddress);
    }
    newAddress = addressRepository.findOne(newId);
    if (newAddress == null) {
      return LejiaResult.build(301, "未找到收货地址记录");
    }
    newAddress.setState(1);
    addressRepository.save(newAddress);
    return LejiaResult.build(200, "ok");
  }

  /**
   * 删除收货地址
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public LejiaResult deleteAddress(Long id) {
    Address address = null;
    if (id != null) {
      address = addressRepository.findOne(id);
      if (address == null) {
        return LejiaResult.build(301, "未找到收货地址记录");
      }
      address.setState(2);
      addressRepository.save(address);
      return LejiaResult.build(200, "ok");
    } else {
      return LejiaResult.build(301, "未找到收货地址记录");
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void editOrderAddress(Address address,OnLineOrder onLineOrder) {
    onLineOrder.setAddress(address);
    orderService.editAddress(onLineOrder);
  }

}
