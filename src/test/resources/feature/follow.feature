Feature: follow Article and Constitution

  # Article
  Scenario: The route for follow article must response a 201 and return object
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a POST request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/follows"
    Then the response status code should be 201

  Scenario: The route for get follows of articles must response a 200 and return objects
    Given I have citizen John Smith with id "e3c0b08c-11be-418e-95e0-8596b4402feb"
    When I send a GET request to "/citizens/e3c0b08c-11be-418e-95e0-8596b4402feb/follows/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for unfollow article must response a 204
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a DELETE request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/follows"
    Then the response status code should be 204

  # Constitution
  Scenario: The route for follow constitution must response a 201 and return object
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a POST request to "/constitutions/72aa1ee1-4963-eb44-c9e0-5ce6e0f18f00/follow"
    Then the response status code should be 201

  Scenario: The route for get follows of constitutions must response a 200 and return objects
    Given I have citizen John Smith with id "e3c0b08c-11be-418e-95e0-8596b4402feb"
    When I send a GET request to "/citizens/e3c0b08c-11be-418e-95e0-8596b4402feb/follows/constitutions"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for unfollow constitution must response a 204
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a DELETE request to "/constitutions/72aa1ee1-4963-eb44-c9e0-5ce6e0f18f00/follow"
    Then the response status code should be 204
