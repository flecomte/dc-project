@follow
@disable
Feature: follow Constitution

  # Constitution
  Scenario: The route for follow constitution must response a 201 and return
    Given I have citizen Dmitri Mendeleïev
    And I am authenticated as Dmitri Mendeleïev
    And I have constitution
      | id | f6553f5e-0cf5-476a-b84e-15e52ec9d8f9 |
    When I send a POST request to "/constitutions/f6553f5e-0cf5-476a-b84e-15e52ec9d8f9/follows"
    Then the response status code should be 201

  Scenario: The route for get follows of constitutions must response a 200 and return objects
    Given I have citizen André-Marie Ampère with ID "877b45b0-302d-487d-8944-6d03ccdbd0f8"
    And I am authenticated as André-Marie Ampère
    And I have constitution
      | id | 7f642078-8e74-47fc-9712-0c37531674a0 |
    And I have follow of André-Marie Ampère on constitution "7f642078-8e74-47fc-9712-0c37531674a0"
    When I send a GET request to "/citizens/877b45b0-302d-487d-8944-6d03ccdbd0f8/follows/constitutions"
    Then the response status code should be 200
    And the response should contain object:
      | current_page | 1  |
      | limit        | 50 |

  Scenario: The route for unfollow constitution must response a 204
    Given I have citizen Claude Ptolémée
    And I am authenticated as Claude Ptolémée
    And I have constitution
      | id | 4ea03776-a28c-4202-9b90-db1b942946c1 |
    And I have follow of Claude Ptolémée on constitution "4ea03776-a28c-4202-9b90-db1b942946c1"
    When I send a DELETE request to "/constitutions/4ea03776-a28c-4202-9b90-db1b942946c1/follows"
    Then the response status code should be 204

  Scenario: I can know if I follow an constitution
    Given I have constitution with ID "5141e781-ebcd-4de0-8c2b-7d23d4cd58b5"
    And I have citizen Denis Papin
    And I am authenticated as Denis Papin
    And I have follow of Denis Papin on constitution "5141e781-ebcd-4de0-8c2b-7d23d4cd58b5"
    When I send a GET request to "/constitutions/5141e781-ebcd-4de0-8c2b-7d23d4cd58b5/follows"
    Then the response status code should be 200
    And the JSON should contain:
      | target.id | 5141e781-ebcd-4de0-8c2b-7d23d4cd58b5 |
