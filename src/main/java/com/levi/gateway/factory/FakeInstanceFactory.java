package com.levi.gateway.factory;

import com.github.javafaker.Faker;

public class FakeInstanceFactory {

  static Faker faker = Faker.instance();

    public static Faker getFaker() {
        return faker;
    }
  }

