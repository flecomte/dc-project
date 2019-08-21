Feature: follow Article and Constitution

  # Article
  Scenario: The route for follow article must response a 201 and return object
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "POST" request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/follow"
    Then the response status code should be 201

  Scenario: The route for get follows of articles must response a 200 and return objects
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "GET" request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/follows/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for unfollow article must response a 204
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "DELETE" request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/follow"
    Then the response status code should be 204

  # Constitution
  Scenario: The route for follow constitution must response a 201 and return object
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "POST" request to "/constitutions/72aa1ee1-4963-eb44-c9e0-5ce6e0f18f00/follow"
    Then the response status code should be 201

  Scenario: The route for get follows of constitutions must response a 200 and return objects
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "GET" request to "/citizens/64b7b379-2298-43ec-b428-ba134930cabd/follows/constitutions"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for unfollow constitution must response a 204
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "DELETE" request to "/constitutions/72aa1ee1-4963-eb44-c9e0-5ce6e0f18f00/follow"
    Then the response status code should be 204
