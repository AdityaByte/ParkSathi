"""
Here we mainly deals with firebase admin things.
"""
import firebase_admin
from firebase_admin import credentials, auth
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pathlib import Path

from app.model.user import User, UserType

if not firebase_admin._apps:
    # Initializing the firebase admin SDK.
    service_account_file_path = Path(__file__).parent.parent.parent / "service_account_key.json"
    cred = credentials.Certificate(service_account_file_path)

    firebase_admin.initialize_app(cred)

security = HTTPBearer()

def verify_firebase_token(creds: HTTPAuthorizationCredentials = Depends(security)):
    """Validates the user token and returns the user data."""
    try:
        decoded_token = auth.verify_id_token(creds.credentials)
        return decoded_token
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication Failed: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )

async def get_current_user_role(res: HTTPAuthorizationCredentials = Depends(security)):
    try:
        decoded_token = verify_firebase_token(res)
        uid = decoded_token['uid']

        # Fetching the user from mongodb.
        user = await User.find_one(User.uid == uid)

        if not user:
            raise HTTPException(status_code=404, detail="User not found in database")

        is_owner = UserType.OWNER in user.roles
        return {"uid": uid, "is_owner": is_owner, "roles": user.roles}

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token"
        )