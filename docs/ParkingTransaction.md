# Parking Transaction API Spec

# FOR ADMIN

## Get All Parking Transactions

Endpoint : GET /api/v1/admin/parking-transactions

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
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": "2025-08-01T23:37:23.837236",
            "totalCost": 5000.00,
            "status": "COMPLETED",
            "paymentStatus": "PAID",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:37:29.412214"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 401,
    "status": "Unauthorized",
    "message": "Missing or invalid authorization token.",
    "data": null,
    "error": "Authentication Failed"
}
```



## Get Parking Transaction by ID

Endpoint : GET /api/v1/admin/parking-transactions/{transactionId}

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
        "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
        "vehicle": {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        "parkingSpot": {
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
        "entryTime": "2025-08-01T23:27:02.395791",
        "exitTime": "2025-08-01T23:37:23.837236",
        "totalCost": 5000.00,
        "status": "COMPLETED",
        "paymentStatus": "PAID",
        "createdAt": "2025-08-01T23:27:02.760874",
        "updatedAt": "2025-08-01T23:37:29.412214"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "Not Found",
    "message": "Parking transaction not found.",
    "data": null,
    "error": "Resource Not Found"
}
```



## Get Parking Transactions by Vehicle ID

Endpoint : GET /api/v1/admin/parking-transactions/by-vehicle/{vehicleId}

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
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": "2025-08-01T23:37:23.837236",
            "totalCost": 5000.00,
            "status": "COMPLETED",
            "paymentStatus": "PAID",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:37:29.412214"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "Not Found",
    "message": "No parking transactions found for the specified vehicle ID.",
    "data": null,
    "error": "Resource Not Found"
}
```



## Get Parking Transactions by Parking Spot ID

Endpoint : GET /api/v1/admin/parking-transactions/by-spot/{parkingSpotId}

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
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": "2025-08-01T23:37:23.837236",
            "totalCost": 5000.00,
            "status": "COMPLETED",
            "paymentStatus": "PAID",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:37:29.412214"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "Not Found",
    "message": "No parking transactions found for the specified parking spot ID.",
    "data": null,
    "error": "Resource Not Found"
}
```



## Get Parking Transactions by Merchant ID

Endpoint : GET /api/v1/admin/parking-transactions/by-merchant/{merchantId}

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
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": "2025-08-01T23:37:23.837236",
            "totalCost": 5000.00,
            "status": "COMPLETED",
            "paymentStatus": "PAID",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:37:29.412214"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "Not Found",
    "message": "No parking transactions found for the specified merchant ID.",
    "data": null,
    "error": "Resource Not Found"
}
```



## Get Parking Transactions by Parking Status

Endpoint : GET /api/v1/admin/parking-transactions/by-parking-status?status={status}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Query Parameters :
| Key | Value |
| --- | --- |
| `status` | `COMPLETED`


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": "2025-08-01T23:37:23.837236",
            "totalCost": 5000.00,
            "status": "COMPLETED",
            "paymentStatus": "PAID",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:37:29.412214"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "Bad Request",
    "message": "Invalid or missing 'status' query parameter. Please provide a valid parking status (e.g., COMPLETED, ONGOING).",
    "data": null,
    "error": "Validation Error"
}
```



## Get Parking Transactions by Payment Status

Endpoint : GET /api/v1/admin/parking-transactions/by-payment-status?status={status}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Query Parameters :
| Key | Value |
| --- | --- |
| `status` | `PAID`


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": "2025-08-01T23:37:23.837236",
            "totalCost": 5000.00,
            "status": "COMPLETED",
            "paymentStatus": "PAID",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:37:29.412214"
        }
    ],
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "Bad Request",
    "message": "Invalid or missing 'status' query parameter. Please provide a valid payment status (e.g., PAID, UNPAID).",
    "data": null,
    "error": "Validation Error"
}
```



## Update Parking Transaction Payment Status

Endpoint : PATCH /api/v1/admin/parking-transactions/{transactionId}/payment-status

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Query Parameters :
| Key | Value |
| --- | --- |
| `newPaymentStatus` | `PAID`


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
        "vehicle": {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        "parkingSpot": {
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
        "entryTime": "2025-08-01T23:27:02.395791",
        "exitTime": "2025-08-01T23:37:23.837236",
        "totalCost": 5000.00,
        "status": "COMPLETED",
        "paymentStatus": "PAID",
        "createdAt": "2025-08-01T23:27:02.760874",
        "updatedAt": "2025-08-01T23:37:29.412214"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "Bad Request",
    "message": "Invalid or missing 'newPaymentStatus' query parameter. Please provide a valid payment status (e.g., PAID, UNPAID).",
    "data": null,
    "error": "Validation Error"
}
```



## Cancel Parking Transaction

Endpoint : PATCH /api/v1/admin/parking-transactions/{transactionId}/cancel

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
    "data": null,
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "Not Found",
    "message": "Parking transaction with ID 'acd2fc62-2a71-4c91-824a-ce471b405d70' not found.",
    "data": null,
    "error": "Resource Not Found"
}
```





# FOR USER

## Record Parking Entry

Endpoint : POST /api/v1/user/parking-transactions/entry

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "licensePlate": "L 1990 KO",
  "merchantCode": "O5PGKUMA",
  "spotNumber": "098"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
        "vehicle": {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        "parkingSpot": {
            "id": "f908588e-b30f-492e-8086-af82a8bf337f",
            "spotNumber": "098",
            "spotType": "MOTORCYCLE",
            "status": "OCCUPIED",
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
        "entryTime": "2025-08-01T23:27:02.3957915",
        "exitTime": null,
        "totalCost": null,
        "status": "ACTIVE",
        "paymentStatus": "PENDING",
        "createdAt": null,
        "updatedAt": null
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Vehicle or parking spot not found, or parking spot is already occupied."
}
```



## Record Parking Exit

Endpoint : POST /api/v1/user/parking-transactions/exit

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "licensePlate": "L 1990 KO",
  "merchantCode": "O5PGKUMA",
  "verificationCode": "2758"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
        "vehicle": {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        "parkingSpot": {
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
        "entryTime": "2025-08-01T23:27:02.395791",
        "exitTime": "2025-08-01T23:37:23.8372363",
        "totalCost": 5000,
        "status": "COMPLETED",
        "paymentStatus": "PAID",
        "createdAt": "2025-08-01T23:27:02.760874",
        "updatedAt": "2025-08-01T23:27:02.760874"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Invalid verification code or parking transaction not found."
}
```



## Get User Active Parking Transaction

Endpoint : GET /api/v1/user/parking-transactions/active?licensePlate=<licensePlate>

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Query Parameters :
| Key | Value |
| --- | --- |
| `licensePlate` | `L 1990 KO`


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
        "vehicle": {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        "parkingSpot": {
            "id": "f908588e-b30f-492e-8086-af82a8bf337f",
            "spotNumber": "098",
            "spotType": "MOTORCYCLE",
            "status": "OCCUPIED",
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
        "entryTime": "2025-08-01T23:27:02.395791",
        "exitTime": null,
        "totalCost": null,
        "status": "ACTIVE",
        "paymentStatus": "PENDING",
        "createdAt": "2025-08-01T23:27:02.760874",
        "updatedAt": "2025-08-01T23:27:02.760874"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "NOT_FOUND",
    "message": "No active parking transaction found for the specified license plate."
}
```



## Get User Parking Transaction History

Endpoint : GET /api/v1/user/parking-transactions/history?licensePlate=<licensePlate>

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Query Parameters :
| Key | Value |
| --- | --- |
| `licensePlate` | `L 1990 KO`


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": [
        {
            "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
            "vehicle": {
                "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
                "licensePlate": "L 1990 KO",
                "type": "MOTORCYCLE",
                "owner": {
                    "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                    "username": "laksabayu18@gmail.com",
                    "email": "laksabayu18@gmail.com"
                }
            },
            "parkingSpot": {
                "id": "f908588e-b30f-492e-8086-af82a8bf337f",
                "spotNumber": "098",
                "spotType": "MOTORCYCLE",
                "status": "OCCUPIED",
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
            "entryTime": "2025-08-01T23:27:02.395791",
            "exitTime": null,
            "totalCost": null,
            "status": "ACTIVE",
            "paymentStatus": "PENDING",
            "createdAt": "2025-08-01T23:27:02.760874",
            "updatedAt": "2025-08-01T23:27:02.760874"
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
    "message": "No transaction history found for the specified license plate."
}
```



## Get User Parking Transaction Details

Endpoint : GET /api/v1/user/parking-transactions/details/{transactionId}

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Query Parameters :
| Key | Value |
| --- | --- |
| `licensePlate` | `L 1990 KO`


Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "success",
    "data": {
        "id": "acd2fc62-2a71-4c91-824a-ce471b405d70",
        "vehicle": {
            "id": "0928ee59-f75e-45a5-9516-cea48441ad10",
            "licensePlate": "L 1990 KO",
            "type": "MOTORCYCLE",
            "owner": {
                "id": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
                "username": "laksabayu18@gmail.com",
                "email": "laksabayu18@gmail.com"
            }
        },
        "parkingSpot": {
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
        "entryTime": "2025-08-01T23:27:02.395791",
        "exitTime": "2025-08-01T23:37:23.837236",
        "totalCost": 5000.00,
        "status": "COMPLETED",
        "paymentStatus": "PAID",
        "createdAt": "2025-08-01T23:27:02.760874",
        "updatedAt": "2025-08-01T23:37:29.412214"
    },
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 404,
    "status": "NOT_FOUND",
    "message": "Parking transaction with ID not found."
}
```