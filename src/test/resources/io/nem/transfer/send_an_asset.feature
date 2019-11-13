Feature: Send an asset
  As Alice
  I want to send 1 concert ticket to Bob
  So that he can attend the event.

Background: Create assets for transfer.
    Given Alice registered the asset "X"
    And Alice registered the asset "Y"

  @bvt
  Scenario Outline: An account sends an asset to another account
    When Alice sends <amount> asset "<asset>" to Bob
    Then she should receive a confirmation message
    And Bob should receive <amount> of asset "<asset>"
    And Alice "<asset>" balance should decrease by <amount> unit

    Examples:
      | amount | asset |
      | 1      | X     |
      | 2      | Y     |

  Scenario: An account sends an asset to itself
    When Alice sends 1 asset "X" to Alice
    Then she should receive a confirmation message
    And Alice balance should remain intact

  @bvt
  Scenario Outline: An account tries to send an asset to an invalid account
    When Alice tries to send 1 asset "Y" to <recipient>
    Then she should receive the error "<error>"
    And Alice balance should remain intact
    Examples:
      | recipient                                      | error                        |
      | NAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3HT | Failure_Core_Invalid_Address |
      | MAIBV5-BKEVGJ-IZQ4RP-224TYE-J3ZIUL-WDHUTI-X3H5 | Failure_Core_Invalid_Address |

  @bvt
  Scenario Outline: An account tries to send assets that does not have
    When Alice tries to send <amount> asset "<asset>" to Sue
    Then she should receive the error "<error>"
    And Alice balance should remain intact

    Examples:
      | amount | asset   | error                             |
      | -1     | X       | Failure_Core_Insufficient_Balance |
      | 1      | O       | Failure_Core_Insufficient_Balance |
      | 1      | unknown | Failure_Core_Insufficient_Balance |
      | 105    | Y       | Failure_Core_Insufficient_Balance |

  @bvt
  Scenario: An account sends multiple assets to another account
    When Alice sends 1 asset "X" and 2 asset "Y" to Bob
    Then she should receive a confirmation message
    And Bob should receive 1 of asset "X"
    And Bob should receive 2 of asset "Y"
    And Alice "X" balance should decrease by 1 unit
    And Alice "Y" balance should decrease by 2 units

  @bvt
  Scenario Outline: An account tries to send multiple assets to another account but at least one of the attached assets can't be sent
    When Alice tries to send <amount> asset "<asset>" and 1 asset "Y" to "Bob"
    Then she should receive the error "<error>"
    And Alice balance should remain intact

    Examples:
      | amount | asset  | error                                 |
      | 500    | X      | Failure_Core_Insufficient_Balance     |
      | 1      | U      | Failure_Core_Insufficient_Balance     |
      | 1      | Y      | Failure_Transfer_Out_Of_Order_Mosaics |

  @bvt
  Scenario: An account sends a non-transferable asset to the account that registered the asset
    Given Alice registers a non transferable asset which she transfer 10 asset to Sue
    When Sue transfer 1 asset to Alice
    Then 1 asset transfered successfully

  @bvt
  Scenario: An account tries to send a non-transferable asset to another account
    Given Alice registers a non transferable asset which she transfer 10 asset to Sue
    When Sue transfer 1 asset to Bob
    Then she should receive the error "Failure_Mosaic_Non_Transferable"

  @bvt
  Scenario: An account transfer a transferable asset to another account
    Given Alice registers a transferable asset which she transfer asset to Bob
    When Bob transfer 10 asset to Sue
    Then 10 asset transfered successfully

  @bvt
  Scenario: An account tries to send an expired asset
  Given Alice has registered expiring asset for 2 blocks
  And the asset is now expired
  When Alice transfer 1 asset to Bob
  Then she should receive the error "Failure_Core_Insufficient_Balance"
