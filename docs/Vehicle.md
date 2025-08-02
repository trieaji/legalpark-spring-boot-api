# Vehicle API Spec

# FOR ADMIN

## Register Vehicle

Endpoint : POST /api/v1/admin/vehicle/register

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "licensePlate": "L 251 KO",
  "type": "CAR",
  "ownerId": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "a10706d6-60ef-4b52-b479-35276113ea5d",
        "licensePlate": "L 251 KO",
        "type": "CAR",
        "owner": {
            "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
            "username": "laksabayu18@gmail.com",
            "email": "laksabayu18@gmail.com"
        }
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "User with ID not found or license plate already registered."
}
```



## Get All Vehicle

Endpoint : GET /api/v1/admin/vehicles

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "a10706d6-60ef-4b52-b479-35276113ea5d",
            "licensePlate": "L 251 KO",
            "type": "CAR",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 401,
    "status": "UNAUTHORIZED",
    "message": "Invalid or expired token."
}
```



## Get Vehicle by ID

Endpoint : GET /api/v1/admin/vehicle/{id}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "a10706d6-60ef-4b52-b479-35276113ea5d",
        "licensePlate": "L 251 KO",
        "type": "CAR",
        "owner": {
            "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
            "username": "laksabayu18@gmail.com",
            "email": "laksabayu18@gmail.com"
        }
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "NOT_FOUND",
    "message": "Vehicle with ID not found."
}
```



## Get Merchant By Code

Endpoint : GET /api/v1/admin/vehicle/by-user/{userId}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "a10706d6-60ef-4b52-b479-35276113ea5d",
            "licensePlate": "L 251 KO",
            "type": "CAR",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "NOT_FOUND",
    "message": "User with ID not found."
}
```




# Vehicle API Spec

# FOR USER

## Register Vehicle

Endpoint : POST /api/v1/user/vehicle/register

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "licensePlate": "L 1990 KO",
  "type": "MOTORCYCLE"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
        "licensePlate": "L 1990 KO",
        "type": "MOTORCYCLE",
        "owner": {
            "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
            "username": "laksabayu18@gmail.com",
            "email": "laksabayu18@gmail.com"
        }
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "License plate already registered."
}
```
              


## Update Vehicle

Endpoint : PATCH /api/v1/user/vehicle/{id}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |


Request Body :
``` json
{
  "type": "MOTORCYCLE"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
        "licensePlate": "L 1990 KO",
        "type": "MOTORCYCLE",
        "owner": {
            "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
            "username": "laksabayu18@gmail.com",
            "email": "laksabayu18@gmail.com"
        }
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "NOT_FOUND",
    "message": "Vehicle with ID not found or does not belong to the current user."
}
```

                  

## Get All Vehicle

Endpoint : GET /api/v1/user/vehicles

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        {
            "id": "a10706d6-60ef-4b52-b479-35276113ea5d",
            "licensePlate": "L 251 KO",
            "type": "CAR",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 401,
    "status": "UNAUTHORIZED",
    "message": "Invalid or expired token."
}
```



## Get Vehicle by ID

Endpoint : GET /api/v1/user/vehicle/{id}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
        "licensePlate": "L 1990 KO",
        "type": "MOTORCYCLE",
        "owner": {
            "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
            "username": "laksabayu18@gmail.com",
            "email": "laksabayu18@gmail.com"
        }
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "NOT_FOUND",
    "message": "Vehicle with ID not found or does not belong to the current user."
}
```

