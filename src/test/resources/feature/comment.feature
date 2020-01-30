Feature: comment

  Scenario: Can comment childrens
    Given I am authenticated as John Doe with id "64b7b379-2298-43ec-b428-ba134930cabd"
    And I have article with id "9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b"
    When I send a GET request to "/comments/9226c1a3-8091-c3fa-7d0d-c2e98c9bee7b/children"
    Then the response status code should be 200