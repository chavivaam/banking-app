# Banking Transaction Application
 
## Overview
 
An application that allows users to authenticate,
view transactions, and create transactions.
 
The system supports role-based access control (RBAC):
 
- USER
- ADMIN
 
## Back-End Design
#### Used Technologies:
- Spring Boot
- Spring Security
- Hibernate

### Backend Design Summary

The backend is implemented using Spring Boot and follows a layered architecture consisting of Controllers, Services, Repositories, and a relational database accessed through Spring Data JPA (Hibernate). User and transaction data are persisted in the database through JPA entities, with UserEntity representing authenticated users and TransactionEntity representing banking transactions. Authentication and authorization are handled by Spring Security, using form-based login, session management, and role-based access control (RBAC). User credentials are loaded from the database through a custom CustomUserDetailsService, while passwords are securely stored using a BCryptPasswordEncoder. The UserEntity implements UserDetails, allowing Spring Security to use the entity directly as the authenticated principal and enabling easy access to the logged-in user's identifier and role. Authorization rules are configured through a SecurityFilterChain, ensuring that only authenticated users can access protected resources and that administrative operations are restricted to users with the ADMIN role. The frontend relies on Angular route guards and interceptors for user experience, while all security enforcement remains on the backend.
 
## Business Entities
### HUser
A Hibernate entity that will implement org.springframework.security.core.userdetails for security purposes that will be explained below.
It will contain the fields: 
- Long Id as primary key, 
- String userName, 
- String password that will be saved encoded using org.springframework.security.crypto.password.PasswordEncoder ,
- Role role that will be enumerated for saving in the DB as String.
In addition, it will implement the functions: 
```java
getAuthorities() //returns List.of(
            new SimpleGrantedAuthority("ROLE_" + role.name())
        );
isAccountNonExpired() // returns true
isAccountNonLocked() //returns true
isCredentialsNonExpired() //returns true
isEnabled() //returns true
```
 
#### UserRepository extends JpaRepository<HUser, Long> 
- will get for free from JpaRepository findById, findAll, deleteById, save and etc.
- will declare the function HUser getUserByUsername(String username) that will be used for authentication.
#### UserRepositoryImpl implements UserRepository 
will implement the function getUserByUsername.
 
#### UserDTO
The user model that will be used for display in the front. Will include the fields:
- Long userId
- String username
- Role role
#### CreateUserRequest
Admin users will be able to add new users. The create user request will include:
- String userName
- String password
- Role role
 
#### UserService
will declare the following functions and **UserServiceImpl** will implement them:
- createUser(CreateUserRequest request) -> UserDTO
- getCurrentUser() -> HUser
Will get the logged in user using the SecurityContextHolder:
```java
HUser user = (HUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
```
The conversion to HUser possible because HUser implements UserDetails.
 
#### UserController
will declare the following functions and **UserControllerImpl** will implement them as routes handlers:
- /create with the function create(CreateUserRequest) -> UserDTO
- /getAllUsers with the function getAllUsers() -> List<UserDTO>
- These routes will be protected, along with /transaction/** restricted routes inside org.springframework.security.web.SecurityFilterChain, will be explained below.
 
### HTransaction
A Hibernate entity that will contain the fiels:
- Long id as primary key
- Long userId
- Double amount
- TransactionType type
- String description
- Calender createdDate
 
### TransactionRepository extends JpaRepository<HTransaction, Long> 
- will declare the function: getAllTransactionByUserId(Long) -> List<HTransaction>
- will extend other CRUD functions from JPA
 
### TransactionRepositoryImpl implements TransactionRepository 
will implement the function getAllTransactionByUserId
 
### TransactionDTO
The transaction model that will be used for display in the front. Will include the fields:
- Long id
- Long userId
- String username
- TransactionType type
- Double amount
- String description
 
#### TransactionRequest
will include:
- TransactionType type
- Double amount
- String description
#### TransactionService
will declare the following functions and **TransactionServiceImpl** will implement them:
- processTransaction(TransactionRequest) -> TransactionDTO
- getAllTransactionsForUser(Long userId) -> List<TransactionDTO>
- getAllUsersTransactions() -> List<TransactionDTO>
 
#### TransactionController 
will declare the following functions and **TransactionControllerImpl** will implement them as routes handlers:
- /create create(TransactionRequest request) will call TransactionService.processTransaction(request)
- /getAllTransactions getAllTransactions(@AuthenticationPrincipal principal) 
-  /getAllUsersTransactions getAllUsersTransactions(@AuthenticationPrincipal principal). This route will be protected by org.springframework.security.web.SecurityFilterChain
Both functions will use  the principal to get the HUser to send the userId to the getters functions in TransactionService.
 
## Security Entities
### CustomUserDetailsService
During authentication Spring needs access to our user data model and since we save it in the DB we will provide an override to loadUserByUsername:
```Java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {

        return userRepository.getUserByUsername(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException(username));
    }
}
```
### PasswordEncoder
```Java
@Bean
public PasswordEncoder passwordEncoder(){
     return new BCryptPasswordEncoder();
    } 
```
After Spring gets the user from CustomUserDetailsService, it uses PasswordEncoder to validate the user input password: 
```Java
PasswordEncoder.matches(input, userPassword);
```
### SecurityFilterChain
We want Spring Security to filter requests based on our rules, that's why we will create this bean that on Spring startup, the configuration inside will be used for creating actual security filters for the Spring Security's filter chain thats being executed on runtime.
```Java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
   http.
       authorizeHttpRequest(auth-> auth
       .requestMatchers("/login").permitAll
       .requestMatchers("/user/create").
       .hasRole("ADMIN")
       .requestMatchers("/transaction/getAllUsersTransactions").
       .hasRole("ADMIN")
       .anyRequest()
       .authenticated()
      )
      .formLogin(Customizer.withDefaults())
```
 
 
 
