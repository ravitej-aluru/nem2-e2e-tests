@not-implemented
Feature: Prevent receiving transactions from undesired addresses
  As Alex
  I don't want to receive assets from Bobby
  So that I have control of which accounts can send me transactions

  Background:
    Given the following accounts exist:
      | Alex    |
      | Bobby   |
      | Carol   |
#    And an account can only define up to 512 address filters

  Scenario: An account blocks receiving transactions from a set of addresses
    When Bobby blocks receiving transactions from:
      | Alex   |
      | Carol  |
      And Alex tries to send 1 asset "cat.currency" to Bobby
    Then Bobby should receive a confirmation message
#    And receiving transactions from the stated addresses should be blocked
    And Alex should receive the error "Failure_RestrictionAccount_Address_Interaction_Prohibited"

  Scenario: An account allows only receiving transactions from a set of addresses
    When Bobby only allows receiving transactions from:
      | Alex   |
      And Alex sends 1 asset "cat.currency" to Bobby
      And Carol tries to send 1 asset "cat.currency" to Bobby
    Then Bobby should receive a confirmation message
      And Bobby should receive 1 of asset "cat.currency"
      And Alex "cat.currency" balance should decrease in 1 units
      And Carol should receive the error "Failure_RestrictionAccount_Address_Interaction_Prohibited"
#    And  only receiving transactions from the stated addresses should be allowed

  Scenario: An account unblocks an address
    Given Alex blocked receiving transactions from:
      | signer |
      | Bobby    |
      | Carol  |
    When Alex unblocks "Bobby"
    Then she should receive a confirmation message
    And only Carol should remain blocked

  Scenario: An account removes an address from the allowed addresses
    Given Alex only allowed receiving transactions from "Bobby"
      | signer |
      | Bobby    |
      | Carol  |
    When Alex removes "Bobby" from the allowed addresses
    Then she should receive a confirmation message
    And only Carol should remain allowed

  Scenario: An account unblocks a not blocked address
    Given Alex blocked receiving transactions from "Bobby"
    When Alex unblocks "Carol"
    Then she should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account removes an address that does not exist in the allowed addresses
    Given Alex blocked receiving transactions from "Bobby"
    When Alex removes "Carol" from the allowed addresses
    Then she should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account tries only to allow receiving transactions from a set of addresses when it has blocked addresses
    Given Alex blocked receiving transactions from "Bobby"
    When Alex only allows receiving transactions from "Carol"
    Then she should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account tries to block receiving transactions from a set of addresses when it has allowed addresses
    Given Alex only allowed receiving transactions from "Bobby"
    When Alex blocks receiving transactions from "Carol"
    Then she should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account tries to block an address twice
    Given Alex blocked receiving transactions from "Bobby"
    When Alex blocks receiving transactions from "Bobby"
    Then she should receive the error "Failure_RestrictionAccount_Modification_Redundant"

  Scenario: An account tries to allow an address twice
    Given Alex only allowed receiving transactions from "Bobby"
    When Alex only allows receiving transactions from "Bobby"
    Then she should receive the error "Failure_RestrictionAccount_Modification_Redundant"

  Scenario: An account tries to block herself
    When Alex blocks receiving transactions from herself:
    Then she should receive the error "Failure_RestrictionAccount_Modification_Address_Invalid"

  Scenario: An account tries to only allow itself
    When Alex only allows receiving transactions from herself
    Then she should receive the error "Failure_RestrictionAccount_Modification_Address_Invalid"

  Scenario: An account tries to block too many addresses
    Given Alex blocked receiving transactions from 512 addresses
    When Alex blocks receiving transactions from "Bobby"
    Then she should receive the error "Failure_RestrictionAccount_Values_Count_Exceeded"

  Scenario: An account tries to only allow too many addresses
    Given Alex only allowed receiving transactions from 512 addresses
    When Alex only allows receiving transactions from "Bobby"
    Then she should receive the error "Failure_RestrictionAccount_Values_Count_Exceeded"

  Scenario: An account tries to block too many addresses in a single transaction
    When Alex blocks receiving transactions from 513 addresses
    Then she should receive the error "Failure_RestrictionAccount_Modification_Count_Exceeded"

  Scenario: An account tries to only allow too many addresses in a single transaction
    When Alex only allows receiving transactions from 513 addresses
    Then she should receive the error "Failure_RestrictionAccount_Modification_Count_Exceeded"

  Scenario Outline: An account tries to block an invalid address
    When Alex blocks receiving transactions from "<address>"
    Then she should receive the error "<error>"

    Examples:
      | address                                        | error                        |
      | SAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H  | Failure_Core_Invalid_Address |
      | bo                                             | Failure_Core_Invalid_Address |
      | MAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H5 | Failure_Core_Wrong_Network   |

  Scenario Outline: An account tries only allow transactions from an invalid address
    When Alex only allows receiving transactions from "<address>"
    Then she should receive the error "<error>"

    Examples:
      | address                                        | error                        |
      | SAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H  | Failure_Core_Invalid_Address |
      | bo                                             | Failure_Core_Invalid_Address |
      | MAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H5 | Failure_Core_Wrong_Network   |
