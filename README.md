### KeyUserNotAuthenticated

Tested and reproduced on following devices:

* OnePlus 13R
* OnePlus 15
* Xiaomi 14T
* Motorola Edge Fusion 60 ?


Assumption: 

1. Per-operation auth flow: The Signature object is initialized via initSign(), wrapped in a
   CryptoObject, passed through BiometricPrompt, and then signature.update(message) +
   signature.sign() are called    
   after authentication succeeds.
2. The 256-byte boundary: StrongBox secure elements have limited internal buffer sizes. When you call signature.update() with data that fits
   in the SE's
   internal buffer (≤256 bytes), the operation completes in a single round-trip to the secure
   element, and the per-operation auth token remains valid.
3. When data exceeds 256 bytes: The secure element can't process it in one pass. The TEE/StrongBox
   needs to do multiple update rounds internally. On some OEM implementations (OnePlus being one),
   each
   round-trip to the secure element re-validates the authentication token — and the per-op auth
   token is consumed/invalidated after the first use, causing subsequent internal rounds to fail
   with                
   KeyUserNotAuthenticated (-26).     
asasdasdaasdasdadsasd
