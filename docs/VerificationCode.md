# Parking Transaction API Spec

## Generate and Send Payment Verification Code

Endpoint : POST /api/v1/payment/verification/generate

Request Header :
| Key | Value |
| --- | --- |
| `Authorization` | `Bearer <token>` (Wajib, token JWT dari login) |

Request Body :
``` json
{
  "userId": "dc4927eb-8d99-4435-9d04-9f7f7bea28fc",
  "parkingTransactionId": "acd2fc62-2a71-4c91-824a-ce471b405d70"
}
```

Response Body (Success) :
``` json
{
    "code": 200,
    "status": "OK",
    "message": "Payment verification code sent.",
    "data": null,
    "error": null
}
```

Response Body (Failed) :
``` json
{
    "code": 400,
    "status": "BAD_REQUEST",
    "message": "Active parking transaction not found or invalid for ID: acd2fc62-2a71-4c91-824a-ce471b405d7",
    "data": null,
    "error": "FAILED"
}
```