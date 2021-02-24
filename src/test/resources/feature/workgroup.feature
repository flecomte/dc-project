@workgroup
@disable
Feature: Workgroup

  Scenario: Can get one workgroup
    Given I have citizen Stephen Hawking
    And I have citizen Sadi Carnot with ID "be3b0926-8628-4426-804a-75188a6eb315"
    And I have citizen Joseph Fourier with ID "d9671eca-abaf-4b67-9230-3ece700c1ddb"
    And I am authenticated as Stephen Hawking
    And I have workgroup:
      | id         | ab469134-bf14-4856-b093-ae1aa990f977 |
      | name       | Les Mousquets                        |
      | created_by | Stephen Hawking                      |
    And I have members in workgroup "ab469134-bf14-4856-b093-ae1aa990f977":
      | be3b0926-8628-4426-804a-75188a6eb315 |
      | d9671eca-abaf-4b67-9230-3ece700c1ddb |
    When I send a GET request to "/workgroups/ab469134-bf14-4856-b093-ae1aa990f977"
    Then the response status code should be 200
    And the response should contain object:
      | $.id                                 | ab469134-bf14-4856-b093-ae1aa990f977 |
      | $.name                               | Les Mousquets                        |
      | $.members[0].citizen.name.first_name | Stephen                              |
      | $.members[1].citizen.name.first_name | Sadi                                 |

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

  Scenario: Can delete a workgroup
    Given I have citizen Werner Heisenberg
    And I am authenticated as Werner Heisenberg
    And I have workgroup:
      | id         | ab469134-bf14-4856-b093-ae1aa990f977 |
      | created_by | Werner Heisenberg                    |
    When I send a DELETE request to "/workgroups/ab469134-bf14-4856-b093-ae1aa990f977"
    Then the response status code should be 204
    And The workgroup "ab469134-bf14-4856-b093-ae1aa990f977" not exists

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

  Scenario: Can add member to workgroup
    Given I have citizen Blaise Pascal
    And I have citizen Roger Penrose with ID "6d883fe7-5fc0-4a50-8858-72230673eba4"
    And I have citizen Alessandro Volta with ID "b5bac515-45d4-4aeb-9b6d-2627a0bbc419"
    And I am authenticated as Blaise Pascal
    And I have workgroup:
      | id         | b0ea1922-3bc6-44e2-aa7c-40158998cfbb |
      | name       | Les bonobos                          |
      | created_by | Blaise Pascal                        |
    When I send a POST request to "/workgroups/b0ea1922-3bc6-44e2-aa7c-40158998cfbb/members" with body:
      """
      [
        {
          "citizen": {"id":"6d883fe7-5fc0-4a50-8858-72230673eba4"},
          "roles": ["MASTER"]
        },
        {
          "citizen": {"id":"b5bac515-45d4-4aeb-9b6d-2627a0bbc419"},
          "roles": ["MASTER"]
        }
      ]
      """
    Then the response status code should be 201

  Scenario: Can remove member to workgroup
    Given I have citizen Heinrich Hertz with
      | id | 94f92424-c257-4582-907c-98564a8c4ac9 |
    And I have citizen William Thomson with ID "87909ba3-2069-431c-9924-219fd8411cf2"
    And I have citizen Paul Dirac with ID "1baf48bb-02bc-4d8f-ac86-33335354f5e7"
    And I am authenticated as Heinrich Hertz
    And I have workgroup:
      | id         | b6c975df-dd44-4e99-adc1-f605746b0e11 |
      | name       | Les Tacos                            |
      | created_by | Heinrich Hertz                       |
    And I have members in workgroup "b6c975df-dd44-4e99-adc1-f605746b0e11":
      | 87909ba3-2069-431c-9924-219fd8411cf2 |
      | 1baf48bb-02bc-4d8f-ac86-33335354f5e7 |
    When I send a DELETE request to "/workgroups/b6c975df-dd44-4e99-adc1-f605746b0e11/members" with body:
      """
      [
        {
          "citizen": {"id":"87909ba3-2069-431c-9924-219fd8411cf2"},
          "roles": ["MASTER"]
        }
      ]
      """
    Then the response status code should be 200
    And the JSON should have 2 items
    And the response should contain object:
      | $.[0]citizen.id | 94f92424-c257-4582-907c-98564a8c4ac9 |
      | $.[1]citizen.id | 1baf48bb-02bc-4d8f-ac86-33335354f5e7 |

  Scenario: Can update members on workgroup
    Given I have citizen Leon Foucault
    And I have citizen Sadi Carnot with ID "be3b0926-8628-4426-804a-75188a6eb315"
    And I have citizen Joseph Fourier with ID "d9671eca-abaf-4b67-9230-3ece700c1ddb"
    And I have citizen Georg Ohm with ID "b49e20c1-8393-45d6-a6a0-3fa5c71cbdc1"
    And I am authenticated as Leon Foucault
    And I have workgroup:
      | id         | 784fe6bc-7635-4ae2-b080-3a4743b998bf |
      | name       | Les Tacos                            |
      | created_by | Leon Foucault                        |
    And I have members in workgroup "784fe6bc-7635-4ae2-b080-3a4743b998bf":
      | be3b0926-8628-4426-804a-75188a6eb315 |
      | d9671eca-abaf-4b67-9230-3ece700c1ddb |
    When I send a PUT request to "/workgroups/784fe6bc-7635-4ae2-b080-3a4743b998bf/members" with body:
      """
      [
        {
          "citizen": {"id":"be3b0926-8628-4426-804a-75188a6eb315"},
          "roles": ["MASTER"]
        },
        {
          "citizen": {"id":"b49e20c1-8393-45d6-a6a0-3fa5c71cbdc1"},
          "roles": ["MASTER"]
        }
      ]
      """
    Then the response status code should be 200
    And the JSON should have 2 items
    And the response should contain object:
      | $.[0]citizen.id | be3b0926-8628-4426-804a-75188a6eb315 |
      | $.[1]citizen.id | b49e20c1-8393-45d6-a6a0-3fa5c71cbdc1 |
