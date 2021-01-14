@follow
Feature: follow Article

  # Article
  Scenario: The route for follow article must response a 201
    Given I have citizen Louis Pasteur
    And I am authenticated as Louis Pasteur
    And I have article
      | id | 04754b7b-edef-4adc-af81-75e3aadeebea |
    When I send a POST request to "/articles/04754b7b-edef-4adc-af81-75e3aadeebea/follows"
    Then the response status code should be 201

  Scenario: The route for get follows of articles must response a 200 and return objects
    Given I have citizen Johannes Kepler with ID "e4592d53-3660-4264-8353-ebdbf5d9c41c"
    And I am authenticated as Johannes Kepler
    And I have article
      | id | d743619a-1f6b-4d20-a2d6-8e81f0e6a4c8 |
    And I have follow of Johannes Kepler on article "d743619a-1f6b-4d20-a2d6-8e81f0e6a4c8"
    When I send a GET request to "/citizens/e4592d53-3660-4264-8353-ebdbf5d9c41c/follows/articles"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for unfollow article must response a 204
    Given I have citizen Thomas Edison
    And I am authenticated as Thomas Edison
    And I have article
      | id | aad3aa9d-95fd-4919-9e84-46255f620e31 |
    And I have follow of Thomas Edison on article "aad3aa9d-95fd-4919-9e84-46255f620e31"
    When I send a DELETE request to "/articles/aad3aa9d-95fd-4919-9e84-46255f620e31/follows"
    Then the response status code should be 204

  Scenario: I can know if I follow an article
    Given I have article with ID "3ee4e6d0-f312-4940-872d-1f578c8d824c"
    And I have citizen Marie Curie
    And I am authenticated as Marie Curie
    And I have follow of Marie Curie on article "3ee4e6d0-f312-4940-872d-1f578c8d824c"
    When I send a GET request to "/articles/3ee4e6d0-f312-4940-872d-1f578c8d824c/follows"
    Then the response status code should be 200
    And the JSON should contain:
      | target.id | 3ee4e6d0-f312-4940-872d-1f578c8d824c |
