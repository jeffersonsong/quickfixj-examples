Feature: Functionalities of the executor in in FIX4.2

  Scenario: Buy limit order with fill in FIX4.2 protocol
     When the following messages are sent to the executor:
       | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=14.5^54=1^55=CSCO |
     Then the executor returns following messages:
       | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100^31=0^32=0 |
       | 8=FIX.4.2^35=8^49=B^56=A^6=12.3^11=0001^14=100^20=0^38=100^39=2^54=1^55=CSCO^150=2^151=0^31=12.3^32=100 |
       
  Scenario: Buy limit order with no fill in FIX4.2 protocol
     When the following messages are sent to the executor:
       | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=1^55=CSCO |
     Then the executor returns following messages:
       | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100^31=0^32=0 |     
       
  Scenario: Buy market order with fill in FIX4.2 protocol
     When the following messages are sent to the executor:
       | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=1^54=1^55=CSCO |
     Then the executor returns following messages:
       | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^20=0^38=100^39=0^54=1^55=CSCO^150=0^151=100^31=0^32=0 |
       | 8=FIX.4.2^35=8^49=B^56=A^6=12.3^11=0001^14=100^20=0^38=100^39=2^54=1^55=CSCO^150=2^151=0^31=12.3^32=100 |  
       
  Scenario: Sell limit order with fill in FIX4.2 protocol
     When the following messages are sent to the executor:
       | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=10.5^54=2^55=CSCO |
     Then the executor returns following messages:
       | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^20=0^38=100^39=0^54=2^55=CSCO^150=0^151=100^31=0^32=0 |
       | 8=FIX.4.2^35=8^49=B^56=A^6=12.3^11=0001^14=100^20=0^38=100^39=2^54=2^55=CSCO^150=2^151=0^31=12.3^32=100 |
       
  Scenario: Sell limit order with no fill in FIX4.2 protocol
     When the following messages are sent to the executor:
       | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=2^44=14.5^54=2^55=CSCO |
     Then the executor returns following messages:
       | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^20=0^38=100^39=0^54=2^55=CSCO^150=0^151=100^31=0^32=0 |  
       
  Scenario: Sell market order with fill in FIX4.2 protocol
     When the following messages are sent to the executor:
       | 8=FIX.4.2^35=D^11=0001^49=A^56=B^38=100^40=1^54=2^55=CSCO |
     Then the executor returns following messages:
       | 8=FIX.4.2^35=8^49=B^56=A^6=0^11=0001^14=0^20=0^38=100^39=0^54=2^55=CSCO^150=0^151=100^31=0^32=0 |
       | 8=FIX.4.2^35=8^49=B^56=A^6=12.3^11=0001^14=100^20=0^38=100^39=2^54=2^55=CSCO^150=2^151=0^31=12.3^32=100 |                    