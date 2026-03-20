"""
Here we mainly deals with firebase admin things.
"""
import firebase_admin
from firebase_admin import credentials, auth
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pathlib import Path

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