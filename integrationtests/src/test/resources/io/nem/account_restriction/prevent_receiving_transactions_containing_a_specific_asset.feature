Feature: Prevent receiving transactions containing a specific asset
  As Bob,
  I only want to receive "cat.currency" assets
  So that I can ensure I don't own assets unrelated with my activity

  Background:
    Given Alice has the following assets registered and active:
      | ticket           |
      | voucher          |
      | cat.currency     |
#    And an account can only define up to 512 mosaic filters

  Scenario: An account blocks receiving transactions containing a specific asset
    Given Bob blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    When Alice tries to send 1 asset "ticket" to Bob
    Then Bob should receive a confirmation message
    And Alice should receive the error "Failure_RestrictionAccount_Mosaic_Transfer_Prohibited"
#    And Bob balance should remain intact
#    And Alice balance should remain intact

  Scenario Outline: An account allows receiving transactions containing a specific asset
    When Bob allows receiving transactions containing the following assets:
      | <asset> |
    And Alice sends <amount> asset "<asset>" to Bob
    Then Bob should receive a confirmation message
    And receiving the stated assets should be allowed
    And Bob should receive <amount> of asset "<asset>"
    And Alice "<asset>" balance should decrease in <amount> units

    Examples:
      | amount | asset        |
      | 1      | cat.currency |

  Scenario: An account unblocks an asset
    Given Bob blocks receiving transactions containing the following assets:
      | ticket  |
      | voucher |
    When Bob unblocks "ticket"
    And Alice tries to send 1 asset "voucher" to Bob
    Then Bob should receive a confirmation message
#     And receiving "voucher" assets should remain blocked
      # This can be confirmed when Alice receives below error when she tries send a "voucher" asset to Bob.
    And Alice should receive the error "Failure_RestrictionAccount_Mosaic_Transfer_Prohibited"

### Is this scenario actually valid? By default, all assets are allowed. Adding a "BLOCK_XXXX" modification
#   actually blocks the asset.
  Scenario: An account removes an asset from the allowed assets
    Given Bob has only allowed receiving the following assets
      | ticket  |
      | voucher |
    When Bob removes "ticket" from the allowed assets
    And Alice sends 1 asset "voucher" to Bob
    Then Bob should receive a confirmation message
#      And only receiving "voucher" assets should remain allowed
      # This can be confirmed when Alice successfully sends a "voucher" asset to Bob.
    And Bob should receive 1 of asset "voucher"

  Scenario: An account unblocks a not blocked asset
    Given Bob has blocked receiving "ticket" assets
    When Bob tries to unblock receiving "voucher" assets
    Then Bob should receive the error "Failure_RestrictionAccount_Invalid_Modification"

  Scenario: An account removes an asset that does not exist in the allowed assets
    Given Bob has blocked receiving "ticket" assets
    When Bob tries to remove "voucher" from allowed assets
    Then Bob should receive the error "Failure_RestrictionAccount_Invalid_Modification"

  Scenario: An account tries only to allow receiving transactions containing specific assets when it has blocked assets
    Given Bob has blocked receiving "ticket" assets
    When Bob tries to only allow receiving "voucher" assets
    Then Bob should receive the error "Failure_RestrictionAccount_Invalid_Modification"

  Scenario: An account tries to block receiving transactions containing specific assets when it has allowed assets
    Given Bob has only allowed receiving "ticket" assets
    When Bob tries to block receiving "voucher" assets
    Then Bob should receive the error "Failure_RestrictionAccount_Invalid_Modification"

  Scenario: An account tries to block an asset twice
    Given Bob has blocked receiving "ticket" assets
    When Bob tries to block receiving "ticket" assets
    Then Bob should receive the error "Failure_RestrictionAccount_Invalid_Modification"

  Scenario: An account tries to allow an asset twice
    Given Bob has only allowed receiving "ticket" assets
    When Bob tries to only allow receiving "ticket" assets
    Then Bob should receive the error "Failure_RestrictionAccount_Invalid_Modification"

  Scenario: An account tries to block too many mosaics
    Given Bob has blocked receiving 512 different assets
    When Bob tries to block receiving "ticket" assets
    Then Bob should receive the error "Failure_RestrictionAccount_Values_Count_Exceeded"

#  Scenario: An account tries to only allow too many mosaics
#    Given Bob has only allowed receiving 512 different assets
#    When Bob only allows receiving "ticket" assets
#    Then Bob should receive the error "Failure_RestrictionAccount_Values_Count_Exceeded"
#
#  Scenario: An account tries to block too many mosaics in a single transaction
#    When Bob blocks receiving 513 different assets
#    Then Bob should receive the error "Failure_RestrictionAccount_Modification_Count_Exceeded"
#
#  Scenario: An account tries to only allow too many mosaics in a single transaction
#    When Bob only allows receiving 513 different assets
#    Then Bob should receive the error "Failure_RestrictionAccount_Modification_Count_Exceeded"
