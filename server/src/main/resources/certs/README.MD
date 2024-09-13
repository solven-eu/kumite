https://dev.to/pryhmez/implementing-spring-security-6-with-spring-boot-3-a-guide-to-oauth-and-jwt-with-nimbus-for-authentication-2lhf

    openssl genpkey -algorithm RSA -out private-key.pem
    openssl rsa -pubout -in private-key.pem -out public-key.pem
    openssl pkcs8 -topk8 -inform PEM -outform PEM -in private-key.pem -out private-key-pkcs8.pem -nocrypt
