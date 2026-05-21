"""
Here we mainly deals with firebase admin things.
"""
import os
import json
import base64
import firebase_admin
from firebase_admin import credentials, auth
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pathlib import Path
from app.model.user import User, UserType

if not firebase_admin._apps:
    firebase_credentials_b64 = os.getenv("FIREBASE_CREDENTIALS_BASE64")

    if firebase_credentials_b64:
        # Production — load from environment variable
        cred_dict = json.loads(base64.b64decode(firebase_credentials_b64).decode("utf-8"))
        cred = credentials.Certificate(cred_dict)
    else:
        # Local development — load from file
        service_account_file_path = Path(__file__).parent.parent.parent / "service_account_key.json"
        cred = credentials.Certificate(service_account_file_path)

    firebase_admin.initialize_app(cred)

security = HTTPBearer()

def verify_firebase_token(creds: HTTPAuthorizationCredentials = Depends(security)):
    """Validates the user token and returns the user data."""
    try:
        id_token = creds if isinstance(creds, str) else creds.credentials
        decoded_token = auth.verify_id_token(id_token)
        return decoded_token
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication Failed: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )

async def get_current_user(res: HTTPAuthorizationCredentials = Depends(security)):
    try:
        decoded_token = verify_firebase_token(res)
        uid = decoded_token['uid']
        user = await User.find_one(User.uid == uid)
        if user is None:
            raise HTTPException(status_code=404, detail="User not found")
        return user
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication Failed: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )