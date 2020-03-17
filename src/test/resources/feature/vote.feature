@vote
Feature: vote Article

  Scenario: Can Vote article
    Given I have citizen Thalès Milet
    And I am authenticated as Thalès Milet
    And I have article with ID "835c5101-ca39-4038-a4e6-da6ee62ca6d5"
    When I send a PUT request to "/articles/835c5101-ca39-4038-a4e6-da6ee62ca6d5/vote" with body:
    """
    {
      "note": 1
    }
    """
    Then the response status code should be 201

  Scenario: Can Vote constitution
    Given I have citizen Gregor Mendel
    And I am authenticated as Gregor Mendel
    And I have constitution with ID "76e79c89-efc1-492d-9e8f-dc9717363a11"
    When I send a PUT request to "/constitutions/76e79c89-efc1-492d-9e8f-dc9717363a11/vote" with body:
    """
    {
      "note": -1
    }
    """
    Then the response status code should be 201

  Scenario: Can get votes of current citizen
    Given I have citizen Carl Gauss with ID "c044823d-e778-4256-9016-b1334bf933d3"
    And I am authenticated as Carl Gauss
    And I have article with ID "7c9286db-470d-448c-aab1-3f0b072213b1"
    And I have an vote 1 on article "7c9286db-470d-448c-aab1-3f0b072213b1" created by Carl Gauss
    When I send a GET request to "/citizens/c044823d-e778-4256-9016-b1334bf933d3/votes/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page   | 1  |
      | limit          | 50 |
      | total          | 1  |
      | result[0].note | 1  |

  Scenario: Can get votes of current citizen by target ids
    Given I have citizen Rosalind Franklin with ID "ab3269f0-877b-46b2-ae1a-e7e7d1c12132"
    And I am authenticated as Rosalind Franklin
    And I have article with ID "4d457f53-b937-4622-9542-d5f689d3716b"
    And I have an vote 1 on article "4d457f53-b937-4622-9542-d5f689d3716b" created by Rosalind Franklin
    And I have article with ID "117ef3e6-a740-4d04-9a4a-a800a5f274b4"
    And I have an vote -1 on article "117ef3e6-a740-4d04-9a4a-a800a5f274b4" created by Rosalind Franklin
    When I send a GET request to "/citizens/ab3269f0-877b-46b2-ae1a-e7e7d1c12132/votes?id=4d457f53-b937-4622-9542-d5f689d3716b&id=117ef3e6-a740-4d04-9a4a-a800a5f274b4"
    Then the response status code should be 200
    And the response should contain object:
      | [0].note | -1 |
      | [1].note | 1  |

  Scenario: Can vote a comment
    Given I have citizen Antoine Lavoisier
    And I am authenticated as Antoine Lavoisier
    And I have article with ID "54428366-e71e-4961-876c-8a13df5e4b41"
    And I have comment created by Antoine Lavoisier on article "54428366-e71e-4961-876c-8a13df5e4b41":
      | id | e793eccc-456b-4450-a292-46d592229b74 |
    When I send a PUT request to "/comments/e793eccc-456b-4450-a292-46d592229b74/vote" with body:
    """
    {
      "note": -1
    }
    """
    Then the response status code should be 201
    And the response should contain object:
      | down | 1 |
