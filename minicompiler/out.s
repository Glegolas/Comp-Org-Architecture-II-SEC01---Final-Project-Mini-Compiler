.intel_syntax noprefix
.globl main
main:
    push rbp
    mov rbp, rsp
    sub rsp, 16
    mov eax, 1
    mov DWORD PTR [rbp-4], eax
    mov eax, 4
    add eax, DWORD PTR [rbp-4]
    mov DWORD PTR [rbp-8], eax
    mov eax, DWORD PTR [rbp-4]
    add eax, DWORD PTR [rbp-8]
    mov DWORD PTR [rbp-4], eax
    mov eax, 0
    leave
    ret
