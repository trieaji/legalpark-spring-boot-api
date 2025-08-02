# Auth API Spec

## Register User / Admin

Endpoint : POST /api/v1/auth/register

Request Body :

``` json 
{
  "accountName": "Laksa",
  "email": "laksabayu18@gmail.com",
  "phoneNumber": "085738659381",
  "password": "laksa12345"
}
```

Response Body (Success) :
``` json 
{
  "code": 200,
  "status": "OK",
  "message": "success",
  "data": {
    "email": "laksabayu18@gmail.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleWp3bG...YgSufAEzYeg-c"
  },
  "error": null
}
```

Response Body (Failed) :
``` json
{
  "errors": "Email already exists"
}
 ```
  

## Verification Account

Endpoint : POST /api/v1/auth/verification-account

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "code": "3556"
}
 ```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
        "accountName": "Laksa",
        "email": "laksabayu18@gmail.com",
        "phoneNumber": "085738659381",
        "role": "USER",
        "accountStatus": "ACTIVE",
        "balance": 100000.00,
        "createdAt": "2025-08-01T09:46:47.43775",
        "updatedAt": "2025-08-01T09:49:11.354333",
        "enabled": true,
        "username": "laksabayu18@gmail.com"
    },
    "error": null
}
 ```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Verification code is invalid or has expired."
}
 ```



## Login

Endpoint : POST /api/v1/auth/login

Request Body :
``` json
{
  "email": "laksabayu18@gmail.com",
  "password": "laksa12345"
}
 ```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "email": "laksabayu18@gmail.com",
        "role": "USER",
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsYWtzYWJheXUxOEBnbWFpbC5jb20iLCJpYXQiOjE3NTQwMTY1MTAsImV4cCI6MTc1NDE4OTMxMH0.320ojYZQbThSqWNiuuN9ACkwNKpPeDChSfvrB94MNuk",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkYzQ5MjdlYi04ZDk5LTQ0MzUtOWQwNC05ZjdmN2JlYTI4ZmMiLCJpYXQiOjE3NTQwMTY1MTAsImV4cCI6MTc1NDE4OTMxMH0.doBHJxKUmfLaoZ9Ogvb6wWVJnXGLxhSJ4BTgbgck_oI"
    },
    "error": null
}
 ```

Response Body (Failed, 401 Unauthorized)  :
``` json
{
  "errors": "Invalid email or password"
}
 ```



## Logout

Endpoint: POST /api/v1/auth/logout

Request Headers :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib) |

Response Body (Success) :
``` json
{
  "code": 200,
  "status": "OK",
  "message": "Logout successful. Your virtual balance has been reset to 100k.",
  "data": null,
  "error": null
}
 ```