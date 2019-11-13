Feature: Prevent sending transactions by type
  As Alex
  I want to prevent sending assets from my account
  So that I can ensure I don't send any of my assets while accepting escrow contracts

  Background:
    Given the following accounts exist:
      | Alex  |
      | Bobby |
      | Carol |
    Given the following transaction types are available:
      | TRANSFER                       |
      | REGISTER_NAMESPACE             |
      | ACCOUNT_PROPERTIES_ENTITY_TYPE |
#    And an account can only define up to 512 transaction type filters

    # We are using three transaction types for better comprehension.
    # To get all the available transaction types, see the NEM Developer Center/Protocol/Transaction.

  Scenario: 1. An account blocks sending transactions of a given transaction type
    Given Alex blocks sending transactions of type:
      | TRANSFER           |
      | REGISTER_NAMESPACE |
#    When Alex tries to send a transfer
    When Alex tries to send 1 asset "cat.currency" to Bobby
    And Alex tries to register a namespace named "alexexp" for 10 blocks
    Then Alex should receive the error "FAILURE_RESTRICTIONACCOUNT_OPERATION_TYPE_PROHIBITED"
    And Alex should receive the error "FAILURE_RESTRICTIONACCOUNT_OPERATION_TYPE_PROHIBITED"
    And Alex balance should remain intact
    And Bobby balance should remain intact
 #      We must try some other transaction type like REGISTER_MOSAIC etc. here and expect a pass
 #   And sending transactions with the stated transaction types should be blocked

  Scenario: 2. An account only allows sending transactions of a given transaction type
    Given Alex only allows sending transactions of type:
      | TRANSFER           |
      | REGISTER_NAMESPACE |
    When Alex sends 1 asset "cat.currency" to Bobby
    And Alex registers a namespace named "alexexp" for 10 blocks
    Then Alex should receive a confirmation message
    And Alex should become the owner of the new namespace alexexp for least 10 block
    And Bobby should receive 1 of asset "cat.currency"
    And Alex "cat.currency" balance should decrease by 1 units
#      We must try some other transaction type like REGISTER_MOSAIC etc. here and expect a failure
#    And  only sending transactions with the stated transaction types should be allowed

  Scenario: 3. An account unblocks a transaction type
    Given Alex blocks sending transactions of type:
      | TRANSFER           |
      | REGISTER_NAMESPACE |
    When Alex unblocks "TRANSFER" transaction type
    And Alex sends 1 asset "cat.currency" to Bobby
    And Alex tries to register a namespace named "alexexp" for 10 blocks
    Then Bobby should receive a confirmation message
    And Bobby should receive 1 of asset "cat.currency"
    And Alex "cat.currency" balance should decrease by 1 units
    And Alex should receive the error "FAILURE_RESTRICTIONACCOUNT_OPERATION_TYPE_PROHIBITED"
#      And only "REGISTER_NAMESPACE" should remain blocked

  Scenario: 4. An account removes a transaction type from the allowed transaction types
    Given Alex only allows sending transactions of type:
      | TRANSFER           |
      | REGISTER_NAMESPACE |
    When Alex removes "TRANSFER" from the allowed transaction types
    And Alex sends 1 asset "cat.currency" to Bobby
    And Alex tries to register a namespace named "alexexp" for 10 blocks
    Then Alex should receive a confirmation message
    And Bobby should receive 1 of asset "cat.currency"
    And Alex "cat.currency" balance should decrease by 1 units
    And Alex should get should receive the error "FAILURE_RESTRICTIONACCOUNT_OPERATION_TYPE_PROHIBITED"


  Scenario: 5. An account unblocks a not blocked transaction type
#    Given Alex blocked sending "TRANSFER" transactions
    Given Alex blocks sending transactions of type:
      | TRANSFER |
    When Alex unblocks "REGISTER_NAMESPACE" transaction type
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: 6. An account removes a transaction type that does not exist in the allowed transaction types
#    Given Alex blocked sending "TRANSFER" transactions
    Given Alex only allows sending transactions of type:
      | TRANSFER |
    When Alex removes "REGISTER_NAMESPACE" from the allowed transaction types
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: 7. An account tries to only allow sending transactions of a given type when it has blocked types
#    Given Alex blocked sending "TRANSFER" transactions
    Given Alex blocks sending transactions of type:
      | TRANSFER |
#    When Alex only allows sending "REGISTER_NAMESPACE" transactions
    When Alex tries to only allow sending transactions of type:
      | REGISTER_NAMESPACE |
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: 8. An account tries to block sending transactions with a given type when it has allowed types
#    Given Alex only allowed sending "TRANSFER" transactions
    Given Alex only allows sending transactions of type:
      | TRANSFER |
#    When Alex blocks sending "REGISTER_NAMESPACE" transactions
    When Alex tries to block sending transactions of type:
      | REGISTER_NAMESPACE |
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: 9. An account tries to block a transaction type twice
#    Given Alex blocked sending "TRANSFER" transactions
    Given Alex blocks sending transactions of type:
      | TRANSFER |
#    When Alex blocks sending "TRANSFER" transactions
    When Alex tries to block sending transactions of type:
      | TRANSFER |
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Redundant"

  Scenario: 10. An account tries to allow a transaction type twice
#    Given Alex only allowed sending "TRANSFER" transactions
    Given Alex only allows sending transactions of type:
      | TRANSFER |
#    When Alex only allows sending "TRANSFER" transactions
    When Alex tries to only allow sending transactions of type:
      | TRANSFER |
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Redundant"

  Scenario: 11. An account tries to block a transaction with "ACCOUNT_PROPERTIES_ENTITY_TYPE" type
#    When Alex blocks sending "ACCOUNT_PROPERTIES_ENTITY_TYPE" transactions
    Given Alex blocks sending transactions of type:
      | ACCOUNT_PROPERTIES_ENTITY_TYPE |
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"
