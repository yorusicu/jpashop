package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Embeddable
@Getter @Setter
public class Addresses {

    private String city;
    private String street;
    private String zipcode;
}
