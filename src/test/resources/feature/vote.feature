Feature: vote Article

  Scenario: Can Vote article
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a PUT request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/vote" with body:
    """
    {
      "note": 1
    }
    """
    Then the response status code should be 201

  Scenario: Can Vote constitution
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a PUT request to "/constitutions/64b1f265-bfb3-332b-eef9-d00f63a3beaa/vote" with body:
    """
    {
      "note": -1
    }
    """
    Then the response status code should be 201

  Scenario: Can get votes of current citizen
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/votes/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page   | 1  |
      | limit          | 50 |
      | total          | 1  |
      | result[0].note | 1  |

  Scenario: Can get votes of current citizen by target ids
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/votes?id=9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b&id=64b1f265-bfb3-332b-eef9-d00f63a3beaa"
    Then the response status code should be 200
    And the response should contain object:
      | [0].note | 1 |

  Scenario: Can vote a comment on article
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have comment "ea5c9e87-c99e-4646-a381-2910219e077f" on article "cc9c624e-a27e-42de-af78-ae821c657a68"
    When I send a PUT request to "/articles/cc9c624e-a27e-42de-af78-ae821c657a68/comments/ea5c9e87-c99e-4646-a381-2910219e077f/vote" with body:
    """
    {
      "note": -1
    }
    """
    Then the response status code should be 201
