@opinion
Feature: Opinion

  Scenario: Can get one opinion Choices
    When I send a GET request to "/opinions/6e978eb5-3c48-0def-b093-e01f43983adb"
    Then the response status code should be 200
    And the JSON should contain:
      | name | Opinion1 |

  Scenario: Can get all opinion choices
    When I send a GET request to "/opinions"
    Then the response status code should be 200
    And the JSON should contain:
      | [0]name | Opinion1 |

  Scenario: Can create opinion on article
    Given I have citizen Isaac Newton
      | id | 2f414045-95d9-42ca-a3a9-8cdde52ad253 |
    And I am authenticated as Isaac Newton
    And I have article
      | id        | 9226c1a3-8091-c3fa-7d0d-c2e98c9bee7 |
      | createdBy | Isaac Newton                        |
    And I have an opinion
      | opinion   | Opinion1                             |
      | article   | 9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b |
      | createdBy | Isaac Newton                         |
    When I send a PUT request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/opinions" with body:
    """
    {
      "opinion_choice": "6e978eb5-3c48-0def-b093-e01f43983adb"
    }
    """
    Then the response status code should be 201

  Scenario: Can I get all opinions of citizen filtered by target ids
    When I send a GET request to "/citizens/6434f4f9-f570-f22a-c134-8668350651ff/opinions?id=9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    Then the response status code should be 200
    And the JSON should contain:
      | [0].name | Opinion2 |

  Scenario: Can recieve opinion aggregation with article
    When I send a GET request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    Then the response status code should be 200
    And the JSON should contain:
      | opinions.Opinion2 | 1 |

  Scenario: Can get all opinion of one citizen
    Given I have citizen Albert Einstein
      | id | c1542096-3431-432d-8e35-9dc071d4c818 |
    And I am authenticated as Albert Einstein
    And I have an opinion
      | opinion   | Opinion1                             |
      | article   | 9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b |
      | createdBy | Albert Einstein                      |
    When I send a GET request to "/citizens/c1542096-3431-432d-8e35-9dc071d4c818/opinions/articles"
    Then the response status code should be 200
    And the JSON element result should have 1 items
    And the JSON should contain:
      | result[0].name | Opinion1 |
