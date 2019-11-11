Feature: Prevent receiving transactions from undesired addresses
  As Alex
  I don't want to receive assets from Bobby
  So that I have control of which accounts can send me transactions
#  TODO: this and other restriction features do not have scenarios considering the direction of allow/block.
#   Scenario:
#  ALLOW_INCOMING_ADDRESS - done, ALLOW_OUTGOING_ADDRESS - not done!
  Background:
    Given the following accounts exist:
      | Alex    |
      | Bobby   |
      | Carol   |
#       And an account can only define up to 512 address filters

  Scenario: An account blocks receiving transactions from a set of addresses
    Given Bobby blocks receiving transactions from:
      | Alex   |
      | Carol  |
    When Alex tries to send 1 asset "cat.currency" to Bobby
    Then Bobby should receive a confirmation message
#       And receiving transactions from the stated addresses should be blocked
    And Alex should receive the error "Failure_RestrictionAccount_Address_Interaction_Prohibited"

  Scenario: An account only allows receiving transactions from a set of addresses
    Given Bobby only allowed receiving transactions from:
      | Alex   |
    When Alex sends 1 asset "cat.currency" to Bobby
      And Carol tries to send 1 asset "cat.currency" to Bobby
    Then Bobby should receive a confirmation message
      And Bobby should receive 1 of asset "cat.currency"
    And Alex "cat.currency" balance should decrease by 1 unit
      And Carol should receive the error "Failure_RestrictionAccount_Address_Interaction_Prohibited"
#    And  only receiving transactions from the stated addresses should be allowed

  Scenario: An account unblocks an address
    Given Bobby blocked receiving transactions from:
      | Alex  |
      | Carol  |
    When Bobby unblocks Alex address
    And Alex sends 1 asset "cat.currency" to Bobby
    And Carol tries to send 1 asset "cat.currency" to Bobby
    Then Bobby should receive a confirmation message
    And Bobby should receive 1 of asset "cat.currency"
    And Alex "cat.currency" balance should decrease by 1 unit
    And Carol should receive the error "Failure_RestrictionAccount_Address_Interaction_Prohibited"
#    And only Carol should remain blocked

  Scenario: An account removes an address from the allowed addresses
    Given Bobby only allowed receiving transactions from:
      | Alex  |
      | Carol |
    When Bobby removes Alex from the allowed addresses
    And Carol sends 1 asset "cat.currency" to Bobby
    And Alex tries to send 1 asset "cat.currency" to Bobby
    Then Bobby should receive a confirmation message
    And Bobby should receive 1 of asset "cat.currency"
    And Carol "cat.currency" balance should decrease by 1 unit
    And Alex should receive the error "Failure_RestrictionAccount_Address_Interaction_Prohibited"
#    And only Carol should remain allowed - this is achieved by above 6 lines

  Scenario: An account unblocks a not blocked address
    Given Bobby blocked receiving transactions from:
      | Alex |
    When Bobby tries to unblock Carol address
    Then Bobby should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account removes an address that does not exist in the allowed addresses
    Given Bobby only allowed receiving transactions from:
      | Alex |
    When Bobby tries to remove Carol from the allowed addresses
    Then Bobby should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account tries only to allow receiving transactions from a set of addresses when it has blocked addresses
    Given Bobby blocked receiving transactions from:
      | Alex |
    When Bobby tries to only allow receiving transactions from Carol
    Then Bobby should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account tries to block receiving transactions from a set of addresses when it has allowed addresses
    Given Bobby only allowed receiving transactions from:
      | Alex |
    When Bobby tries to block receiving transactions from Carol
    Then Bobby should receive the error "Failure_RestrictionAccount_Modification_Not_Allowed"

  Scenario: An account tries to block an address twice
    Given Alex blocked receiving transactions from:
      | Bobby |
    When Alex tries to block receiving transactions from Bobby
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Redundant"

  Scenario: An account tries to allow an address twice
    Given Alex only allowed receiving transactions from:
      | Bobby |
    When Alex tries to only allow receiving transactions from Bobby
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Redundant"

  Scenario: An account tries to block self
    When Alex tries to block receiving transactions from herself
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Address_Invalid"

  Scenario: An account tries to only allow self
    When Alex tries to only allow receiving transactions from herself
    Then she should receive the error "Failure_RestrictionAccount_Modification_Address_Invalid"

  Scenario: An account tries to block too many addresses
    Given Alex has blocked receiving transactions from 512 different addresses
    When Alex tries to block receiving transactions from Bobby
    Then Alex should receive the error "Failure_RestrictionAccount_Values_Count_Exceeded"

  Scenario: An account tries to only allow too many addresses
    Given Alex has allowed receiving transactions from 512 different addresses
    When Alex tries to only allow receiving transactions from Bobby
    Then Alex should receive the error "Failure_RestrictionAccount_Values_Count_Exceeded"

  Scenario: An account tries to block too many addresses in a single transaction
    Given there are at least 515 different addresses registered
    When Alex tries to block receiving transactions from 513 different addresses
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Count_Exceeded"

  Scenario: An account tries to only allow too many addresses in a single transaction
    Given there are at least 515 different addresses registered
    When Alex tries to only allow receiving transactions from 513 different addresses
    Then Alex should receive the error "Failure_RestrictionAccount_Modification_Count_Exceeded"

  Scenario Outline: An account tries to block an invalid address
    When Alex tries to block receiving transactions from "<address>"
    Then Alex should receive the error "<error>"

    Examples:
      | address                                        | error                        |
      | SAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H  | Failure_Core_Invalid_Address |
      | bo                                             | Failure_Core_Invalid_Address |
      | MAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H5 | Failure_Core_Wrong_Network   |

  Scenario Outline: An account tries only allow transactions from an invalid address
    When Alex tries to only allow receiving transactions from "<address>"
    Then Alex should receive the error "<error>"

    Examples:
      | address                                        | error                        |
      | SAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H  | Failure_Core_Invalid_Address |
      | bo                                             | Failure_Core_Invalid_Address |
      | MAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H5 | Failure_Core_Wrong_Network   |
