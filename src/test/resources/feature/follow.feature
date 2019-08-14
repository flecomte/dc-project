Feature: follow routes

  Scenario: The route for follow article must response a 200 and return object
    Given I have citizen:
      | id        | 64b7b379-2298-43ec-b428-ba134930cabd |
      | firstName | Jaque                                |
      | lastName  | Dupuis                               |
    When I send a "POST" request to "/articles/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/follow" with body:
    """
    """
    Then the response status code should be 201