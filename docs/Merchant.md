# Merchant API Spec

## Create Merchant

Endpoint : POST /api/v1/merchants

Request Body :
``` json
{
  "merchantName": "Alfamart",
  "merchantAddress": "Kaliwaron",
  "contactPerson": "068395215",
  "contactPhone": "07845939751"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "160b562b-12b9-48df-bf65-d717a55417d2",
        "merchantCode": "O5PGKUMA",
        "merchantName": "Alfamart",
        "merchantAddress": "Kaliwaron",
        "contactPerson": "068395215",
        "contactPhone": "07845939751"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Merchant name or code already exists."
}
```



## Get All Merchants

Endpoint : GET /api/v1/merchants/find

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "160b562b-12b9-48df-bf65-d717a55417d2",
            "merchantCode": "O5PGKUMA",
            "merchantName": "Alfamart",
            "merchantAddress": "Kaliwaron",
            "contactPerson": "068395215",
            "contactPhone": "07845939751",
            "createdAt": "2025-08-01T14:35:10.956931",
            "updatedAt": "2025-08-01T14:35:10.956931"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Merchant name or code already exists."
}
```



## Update Merchant

Endpoint : PATCH /api/v1/merchants/update/{id}

Request Body :
``` json
{
    "merchantAddress": "Kaliwaron"    
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "160b562b-12b9-48df-bf65-d717a55417d2",
        "merchantCode": "O5PGKUMA",
        "merchantName": "Alfamart",
        "merchantAddress": "Kaliwaron",
        "contactPerson": "068395215",
        "contactPhone": "07845939751",
        "createdAt": "2025-08-01T14:35:10.956931",
        "updatedAt": "2025-08-01T14:37:24.816905"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Merchant name or code already exists."
}
```



## Get Merchant By Code

Endpoint : POST /api/v1/merchants/get-by-code

Request Body :
``` json
{
    "merchantCode" : "O5PGKUMA"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "160b562b-12b9-48df-bf65-d717a55417d2",
        "merchantCode": "O5PGKUMA",
        "merchantName": "Alfamart",
        "merchantAddress": "Kaliwaron",
        "contactPerson": "068395215",
        "contactPhone": "07845939751"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Merchant name or code already exists."
}
```