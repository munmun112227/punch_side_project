import { describe, it, expect } from 'vitest';
import { encryptData, fetchPublicKey } from '../src/utils/crypto';

describe('crypto util', () => {
  it('throws error if public key is not loaded', () => {
    expect(() => {
      encryptData({ id: 123 });
    }).toThrowError('Public key not loaded');
  });
});
