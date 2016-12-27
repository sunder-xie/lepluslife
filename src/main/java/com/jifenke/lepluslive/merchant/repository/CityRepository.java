package com.jifenke.lepluslive.merchant.repository;

import com.jifenke.lepluslive.merchant.domain.entities.City;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by wcg on 16/3/17.
 */
public interface CityRepository extends JpaRepository<City, Long> {

  Page<City> findAll(Pageable pageable);

  List<City> findByName(String name);

  List<City> findByHotOrderBySidDesc(Integer hot);
}
