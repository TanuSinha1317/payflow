package com.payflow.common;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MoneyTest {
  @Test void parsesExactPaise(){assertEquals(10050,Money.parse("100.50"));assertEquals(100,Money.parse("1"));}
  @Test void rejectsMoreThanTwoDecimals(){assertThrows(ApiException.class,()->Money.parse("1.001"));}
  @Test void rejectsOverflow(){assertThrows(ApiException.class,()->Money.parse("999999999999999999999"));}
  @Test void representsPaiseExactly(){assertEquals(10,Money.parse("0.10"));assertEquals(1025,Money.parse("10.25"));assertEquals(9999,Money.parse("99.99"));}
}

