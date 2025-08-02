# Parking Spot API Spec

# FOR ADMIN

## Create Parking Spot

Endpoint : POST /api/v1/admin/parking-spots

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "spotNumber": "098",
  "floor": 3,
  "spotType": "MOTORCYCLE",
  "merchantCode": "O5PGKUMA"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "f908588e-b30f-492e-8086-af82a8bf337f",
        "spotNumber": "098",
        "spotType": "MOTORCYCLE",
        "status": "AVAILABLE",
        "floor": 3,
        "merchant": {
            "id": "160b562b-12b9-48df-bf65-d717a55417d2",
            "merchantCode": "O5PGKUMA",
            "merchantName": "Alfamart",
            "merchantAddress": "Kaliwaron",
            "contactPerson": "068395215",
            "contactPhone": "07845939751"
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
    "message": "Merchant code not found or parking spot number already exists."
}
```



## Get All Parking Spots

Endpoint : GET /api/v1/admin/parking-spots

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
            "id": "f908588e-b30f-492e-8086-af82a8bf337f",
            "spotNumber": "098",
            "spotType": "MOTORCYCLE",
            "status": "AVAILABLE",
            "floor": 3,
            "merchant": {
                "id": "160b562b-12b9-48df-bf65-d717a55417d2",
                "merchantCode": "O5PGKUMA",
                "merchantName": "Alfamart",
                "merchantAddress": "Kaliwaron",
                "contactPerson": "068395215",
                "contactPhone": "07845939751"
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



## Get Parking Spots by Merchant

Endpoint : GET /api/v1/admin/parking-spots/by-merchant/{merchantIdentifier}

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
            "id": "f908588e-b30f-492e-8086-af82a8bf337f",
            "spotNumber": "098",
            "spotType": "MOTORCYCLE",
            "status": "AVAILABLE",
            "floor": 3,
            "merchant": {
                "id": "160b562b-12b9-48df-bf65-d717a55417d2",
                "merchantCode": "O5PGKUMA",
                "merchantName": "Alfamart",
                "merchantAddress": "Kaliwaron",
                "contactPerson": "068395215",
                "contactPhone": "07845939751"
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
    "message": "Merchant with code not found."
}
```


## Update Parking Spot

Endpoint : PATCH /api/v1/admin/parking-spots/{id}

Request Body :
``` json
{
  "spotNumber": "098",
  "floor": 3,
  "spotType": "car",
  "merchantCode": "O5PGKUMA"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "f908588e-b30f-492e-8086-af82a8bf337f",
        "spotNumber": "098",
        "spotType": "CAR",
        "status": "AVAILABLE",
        "floor": 3,
        "merchant": {
            "id": "160b562b-12b9-48df-bf65-d717a55417d2",
            "merchantCode": "O5PGKUMA",
            "merchantName": "Alfamart",
            "merchantAddress": "Kaliwaron",
            "contactPerson": "068395215",
            "contactPhone": "07845939751"
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
    "message": "Parking spot with ID not found."
}
```



## Get Merchant By Code/Id

Endpoint : GET /api/v1/admin/parking-spots/{id}

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
        "id": "f908588e-b30f-492e-8086-af82a8bf337f",
        "spotNumber": "098",
        "spotType": "MOTORCYCLE",
        "status": "AVAILABLE",
        "floor": 3,
        "merchant": {
            "id": "160b562b-12b9-48df-bf65-d717a55417d2",
            "merchantCode": "O5PGKUMA",
            "merchantName": "Alfamart",
            "merchantAddress": "Kaliwaron",
            "contactPerson": "068395215",
            "contactPhone": "07845939751"
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
    "message": "User with ID not found."
}
```

       

# Parking Spot API Spec

# FOR USER

## Get Available Parking Spots

Endpoint : GET /api/v1/user/parking-spots/available

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |


Request Body  :
```json
{
  "merchantCode": "O5PGKUMA",
  "spotType": "MOTORCYCLE",
  "floor": 3
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "f908588e-b30f-492e-8086-af82a8bf337f",
            "spotNumber": "098",
            "spotType": "MOTORCYCLE",
            "status": "AVAILABLE",
            "floor": 3,
            "merchant": {
                "id": "160b562b-12b9-48df-bf65-d717a55417d2",
                "merchantCode": "O5PGKUMA",
                "merchantName": "Alfamart",
                "merchantAddress": "Kaliwaron",
                "contactPerson": "068395215",
                "contactPhone": "07845939751"
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



## Get Parking Spots by Merchant

Endpoint : GET /api/v1/user/parking-spots/by-merchant/{merchantCode}

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
            "id": "f908588e-b30f-492e-8086-af82a8bf337f",
            "spotNumber": "098",
            "spotType": "MOTORCYCLE",
            "status": "AVAILABLE",
            "floor": 3,
            "merchant": {
                "id": "160b562b-12b9-48df-bf65-d717a55417d2",
                "merchantCode": "O5PGKUMA",
                "merchantName": "Alfamart",
                "merchantAddress": "Kaliwaron",
                "contactPerson": "068395215",
                "contactPhone": "07845939751"
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
    "message": "Merchant with code not found."
}
```