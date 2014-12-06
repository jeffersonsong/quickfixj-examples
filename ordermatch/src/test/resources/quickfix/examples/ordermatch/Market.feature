Feature: Core Functionality for match engine

  Scenario: Add a single limit order to the order book
    When the following messages are sent to the match engine:
      | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=1^55=CSCO |
    Then the match engine returns messages:
      | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^17=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100 |
      
  Scenario: Add a single limit order to the order book, then cancel the order
    When the following messages are sent to the match engine:
      | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=1^55=CSCO |
    Then the match engine returns messages:
      | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^17=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100 |
    When the following messages are sent to the match engine:
      | 8=FIX.4.2^35=F^11=0002^41=0001^49=A^56=B^38=100^40=2^44=10.5^54=1^55=CSCO |
    Then the match engine returns messages:
      | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0002^41=0001^14=0^17=1^20=0^38=100^39=4^54=1^55=CSCO^150=4^151=0 |
      
  Scenario: Add a buy and sell limit order to the order book, they should match
    When the following messages are sent to the match engine:
      | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=1^55=CSCO |
    Then the match engine returns messages:
      | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^17=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100 |
    When the following messages are sent to the match engine:
      | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=2^55=CSCO |
    Then the match engine returns messages:
      | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^17=1^20=0^38=100^39=0^54=2^55=CSCO^150=0^151=100 |
	  | 8=FIX.4.2^35=8^49=B^56=A^6=10.5^11=0001^14=100^17=2^20=0^31=10.5^32=100^38=100^39=2^54=2^55=CSCO^150=2^151=0 |
	  | 8=FIX.4.2^35=8^49=B^56=A^6=10.5^11=0001^14=100^17=3^20=0^31=10.5^32=100^38=100^39=2^54=1^55=CSCO^150=2^151=0 |