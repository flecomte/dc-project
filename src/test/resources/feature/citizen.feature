Feature: citizens routes

  Scenario: The route for get citizens must response a 200
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens"
    Then the response status code should be 200

  Scenario: The route for get one citizen must response a 200 and return citizen
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    When I send a GET request to "/citizens/6434f4f9-f570-f22a-c134-8668350651ff"
    Then the response status code should be 200
    And the response should contain object:
      | id | 6434f4f9-f570-f22a-c134-8668350651ff |
