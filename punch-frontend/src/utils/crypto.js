import forge from 'node-forge';

// Fetch the public key from the backend API to avoid hardcoding in source code.
// For now, we assume an endpoint exists or we inject it at build time.
// Since the user asked not to hardcode keys, let's fetch it dynamically.
let publicKeyObj = null;

export async function fetchPublicKey() {
    try {
        const response = await fetch('/api/punches/public-key');
        const pem = await response.text();
        publicKeyObj = forge.pki.publicKeyFromPem(pem);
    } catch (e) {
        console.error("Failed to load public key", e);
    }
}

export function encryptData(plainObject) {
    if (!publicKeyObj) throw new Error("Public key not loaded");
    const jsonStr = JSON.stringify(plainObject);
    // Use RSA-OAEP with SHA-256
    const encrypted = publicKeyObj.encrypt(jsonStr, 'RSA-OAEP', {
        md: forge.md.sha256.create(),
        mgf1: {
            md: forge.md.sha1.create()
        }
    });
    return forge.util.encode64(encrypted);
}
