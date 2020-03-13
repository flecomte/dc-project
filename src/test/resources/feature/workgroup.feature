@workgroup
Feature: Workgroup

  Scenario: Can get one workgroup
    Given I have citizen Stephen Hawking
    And I am authenticated as Stephen Hawking
    And I have workgroup:
      | id   | ab469134-bf14-4856-b093-ae1aa990f977 |
      | name | Les Mousquets                        |
    When I send a GET request to "/workgroups/ab469134-bf14-4856-b093-ae1aa990f977"
    Then the response status code should be 200
    And the JSON should contain:
      | id   | ab469134-bf14-4856-b093-ae1aa990f977 |
      | name | Les Mousquets                        |

  Scenario: Can create a workgroup
    Given I have citizen Werner Heisenberg
    And I am authenticated as Werner Heisenberg
    When I send a POST request to "/workgroups" with body:
      """
      {
        "id":"f496d86d-6654-4068-91ff-90e1dbcc5f38",
        "name":"Les Bouffons",
        "description":"La vie est belle",
        "anonymous":false
      }
      """
    Then the response status code should be 201
    And the JSON should contain:
      | id          | f496d86d-6654-4068-91ff-90e1dbcc5f38 |
      | name        | Les Bouffons                         |
      | description | La vie est belle                     |
      | anonymous   | false                                |

  Scenario: Can get workgroups list
    Given I have citizen Max Planck
    And I am authenticated as Max Planck
    And I have workgroup:
      | id   | 3fd8edb6-c4b4-4c94-bc75-ddd9b290d32c |
      | name | Les Pissenlits                       |
    When I send a GET request to "/workgroups"
    Then the response status code should be 200
    And the response should contain object:
      | $.result[0]id | 3fd8edb6-c4b4-4c94-bc75-ddd9b290d32c |