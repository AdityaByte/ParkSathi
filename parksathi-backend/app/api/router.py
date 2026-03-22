"""This file mainly handles all the routes of the v1 api"""
from fastapi import APIRouter, Depends

from app.auth.firebase import get_current_user_role, verify_firebase_token
from app.api.endpoints.user import create_user

router = APIRouter()

# Block start - auth routes mapping.

@router.get("/auth/verify-role")
async def handle_verify_role(user_data: dict = Depends(get_current_user_role)):
    return user_data

# Block end - auth routes mapping.




# Block start - user routes mapping.

@router.post("/user/create")
async def handle_create_user(decoded_token: dict = Depends(verify_firebase_token)):
    # Once the user signs up with Google at the client side we get the decoded token.
    # From this token, we need to create a user.
    return await create_user(decoded_token)

# Block end - user routes mapping.



# Block start - health route mapping.

@router.get("/health")
async def handle_health():
    return {"message": "OK"}

# Block end - health route mapping.
