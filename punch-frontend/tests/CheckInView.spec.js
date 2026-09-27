import { mount } from '@vue/test-utils';
import { describe, it, expect, vi } from 'vitest';
import CheckInView from '../src/views/CheckInView.vue';

// Mock dependencies
vi.mock('../src/utils/crypto', () => ({
  fetchPublicKey: vi.fn(),
  encryptData: vi.fn(() => 'mock-encrypted-data')
}));
vi.mock('../src/api/axiosClient', () => ({
  default: {
    post: vi.fn(() => Promise.resolve({ status: 200, data: { punchTime: '2026-01-01' } })),
    get: vi.fn(() => Promise.resolve({ data: [] }))
  }
}));

describe('CheckInView.vue', () => {
  it('renders employee id input', () => {
    const wrapper = mount(CheckInView);
    const input = wrapper.find('input#employeeId');
    expect(input.exists()).toBe(true);
  });

  it('shows alert if employee id is empty and punch is clicked', async () => {
    const wrapper = mount(CheckInView);
    window.alert = vi.fn();
    
    await wrapper.find('button.btn-primary').trigger('click');
    expect(window.alert).toHaveBeenCalledWith('員編不可為空白');
  });
});
