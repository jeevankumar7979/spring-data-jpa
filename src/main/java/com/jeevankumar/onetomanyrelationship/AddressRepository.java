package com.jeevankumar.onetomanyrelationship;

import com.jeevankumar.onetomanyrelationship.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
