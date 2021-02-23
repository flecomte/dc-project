@opinion
@disable
Feature: Opinion

  Scenario: Can get all opinion choices
    Given I have an opinion choice "Opinion1"
    And I have an opinion choice "Opinion2"
    When I send a GET request to "/opinions"
    Then the response status code should be 200
    And the JSON should contain:
      | [0]name | Opinion1 |
      | [1]name | Opinion2 |

  Scenario: Can get one opinion Choices
    Given I have an opinion choice "Opinion3" with ID "347ec243-0e76-4ab5-9884-7bd503cf5ab5"
    When I send a GET request to "/opinions/347ec243-0e76-4ab5-9884-7bd503cf5ab5"
    Then the response status code should be 200
    And the JSON should contain:
      | name | Opinion3 |

  Scenario: Can create opinion on article
    Given I have citizen Isaac Newton with ID "2f414045-95d9-42ca-a3a9-8cdde52ad253"
    And I am authenticated as Isaac Newton
    And I have article
      | id        | 9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b |
      | createdBy | Isaac Newton                         |
    And I have an opinion choice "Opinion4" with ID "0f4f1721-3136-44f1-9f31-1459f3317b15"
    When I send a PUT request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/opinions" with body:
    """
    {
      "ids": [
        "0f4f1721-3136-44f1-9f31-1459f3317b15"
      ]
    }
    """
    Then the response status code should be 201

  Scenario: Can I get all opinions of citizen filtered by target ids
    Given I have article with ID "4eb8a2c6-ba63-4c1e-919f-72017132a54e"
    And I have citizen Albert Jacquard with ID "c6392fc4-56f5-461b-8015-953a4da3029f"
    And I am authenticated as Albert Jacquard
    And I have an opinion choice "Opinion5" with ID "74d6e105-9aa6-4589-8776-82ce260bb6f4"
    And I have an opinion "Opinion5" on article "4eb8a2c6-ba63-4c1e-919f-72017132a54e" created by Albert Jacquard with ID "994660e7-e9f4-4ae9-9290-b34d78663c7a"
    When I send a GET request to "/citizens/c6392fc4-56f5-461b-8015-953a4da3029f/opinions?id=4eb8a2c6-ba63-4c1e-919f-72017132a54e"
    Then the response status code should be 200
    And the JSON should contain:
      | [0].name | Opinion5 |

  Scenario: Can recieve opinion aggregation with article
    Given I have an opinion choice "Opinion6"
    And I have an opinion choice "Opinion7"
    And I have an opinion choice "Opinion8"
    And I have citizen James Watt
    And I have citizen Paul Langevin
    And I have article with ID "bda8940a-6792-4f2b-936a-ba5c805c8487"
    And I have an opinion "Opinion6" on article "bda8940a-6792-4f2b-936a-ba5c805c8487" created by James Watt
    And I have an opinion "Opinion7" on article "bda8940a-6792-4f2b-936a-ba5c805c8487" created by James Watt
    And I have an opinion "Opinion7" on article "bda8940a-6792-4f2b-936a-ba5c805c8487" created by Paul Langevin
    When I send a GET request to "/articles/bda8940a-6792-4f2b-936a-ba5c805c8487"
    Then the response status code should be 200
    And the JSON should contain:
      | opinions.Opinion6 | 1 |
      | opinions.Opinion7 | 2 |

  Scenario: Can get all opinion of one citizen
    Given I have citizen Albert Einstein with ID "c1542096-3431-432d-8e35-9dc071d4c818"
    And I am authenticated as Albert Einstein
    And I have an opinion choice "Opinion9"
    And I have article with ID "8651b530-ac1b-4214-a784-706781371074"
    And I have an opinion "Opinion9" on article "8651b530-ac1b-4214-a784-706781371074" created by Albert Einstein
    When I send a GET request to "/citizens/c1542096-3431-432d-8e35-9dc071d4c818/opinions/articles"
    Then the response status code should be 200
    And the JSON element result should have 1 items
    And the JSON should contain:
      | result[0].name | Opinion9 |
