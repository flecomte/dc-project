Feature: vote Article

  Scenario: Vote article
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a PUT request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/vote" with body:
    """
    {
      "note": 1
    }
    """
    Then the response status code should be 201

  Scenario: Vote article
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a PUT request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/vote" with body:
    """
    {
      "note": -1
    }
    """
    Then the response status code should be 201
