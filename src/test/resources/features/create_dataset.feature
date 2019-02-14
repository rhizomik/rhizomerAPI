# Created by http://rhizomik.net/~roberto/

Feature: Create server dataset
  In order to define a subset of data to explore
  As a data manager
  I want to create a dataset of graphs from the associated server

  Scenario: create dataset with no server or graphs and default values
    Given I login as "user" with password "password"
    When I create a dataset with id "vegetables"
    Then the response status is 201
    And exists a dataset with id "vegetables"

  Scenario: create non-public dataset and try to access it anonymously
    Given I login as "user" with password "password"
    And I create a dataset with id "vegetables"
    And I'm not logged in
    When I retrieve the dataset with id "vegetables"
    Then the response status is 401

  Scenario: create public dataset accessible anonymously
    Given I login as "user" with password "password"
    And I create a public dataset with id "vegetables"
    And I'm not logged in
    When I retrieve the dataset with id "vegetables"
    Then the response status is 200

  Scenario: create non-public dataset and try to access it as admin
    Given I login as "user" with password "password"
    And I create a dataset with id "vegetables"
    And I login as "admin" with password "password"
    When I retrieve the dataset with id "vegetables"
    Then the response status is 200

  Scenario: create non-public dataset and try to access it as another user
    Given Exists a user "user2" with password "password"
    And I login as "user" with password "password"
    And I create a dataset with id "vegetables"
    And I login as "user2" with password "password"
    When I retrieve the dataset with id "vegetables"
    Then the response status is 401