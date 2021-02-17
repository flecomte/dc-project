@comment
@disable
Feature: comment

  Scenario: Can comment childrens
    Given I have citizen John Dalton
    And I am authenticated as John Dalton
    And I have article
      | id | 4c948e8f-eada-4e10-8d7d-7192affe1313 |
    And I have comment created by John Dalton on article "4c948e8f-eada-4e10-8d7d-7192affe1313"
    When I send a GET request to "/comments/4c948e8f-eada-4e10-8d7d-7192affe1313/children"
    Then the response status code should be 200