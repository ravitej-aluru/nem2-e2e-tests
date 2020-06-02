Feature: Associate metadata with a namespace
  As Sarah, I want to be able attach custom data to my namespace that
  will be public available to other who need it.

  Sarah is an admin in her company
  Bob works as a digital notary that stamp accounts on Symbol’s public blockchain.
  He notarize a document, then tags the customer’s account with the digitized document as metadata.

  @bvt
  Scenario: Alice wants to add her educational certificate to her account
    Given Sarah request Bob to notarized her "college certificate"
    And Bob published the bonded contract
    When "Sarah" accepts the transaction
    Then Sarah should have her "college certificate" attached to the account by Bob
