_text SEGMENT

main PROC
    
    mov r8, 0 ; own
    mov r9, 0 ; opponent
    mov r10, 12 ; starting depth
    mov r11, 4432676798593 ; row_0
    mov r12, 279258638311359 ; full
    mov r14, r10 ; depth
    mov r15, 0 ; perft counter

    jmp perft_start

perft_return:
    pop r13
    xchg r8, r9

skip_child:
    mov rax, r13
    neg rax
    and rax, r13
    xor r8, rax ; unmake move
    xor r13, rax ; remove move from moves

loop_start:
    test r13, r13
    jz loop_end ; no moves left
    mov rax, r13
    neg rax
    and rax, r13
    xor r8, rax ; make move
    
    mov rbx, r8
    shr rbx, 2
    and rbx, r8
    mov rax, rbx
    shr rax, 1
    test rax, rbx
    jnz skip_child ; vertical win

    mov rbx, r8
    shr rbx, 14
    and rbx, r8
    mov rax, rbx
    shr rax, 7
    test rax, rbx
    jnz skip_child ; horizontal win

    mov rbx, r8
    shr rbx, 16
    and rbx, r8
    mov rax, rbx
    shr rax, 8
    test rax, rbx
    jnz skip_child ; antidiagonal win

    mov rbx, r8
    shr rbx, 12
    and rbx, r8
    mov rax, rbx
    shr rax, 6
    test rax, rbx
    jnz skip_child ; main diagonal win

    xchg r8, r9
    push r13

perft_start:
    mov r13, r8
    xor r13, r9
    add r13, r11
    and r13, r12 ; moves generated into r13

    dec r14
    jnz loop_start ; loop while moves are left
    popcnt r13, r13
    add r15, r13

loop_end:
    inc r14
    cmp r10, r14
    jne perft_return
    mov rax, r15
    ret ; eax is returned, we lose the upper 32 bits, but it should be enough to check correctness

main ENDP

_text ENDS
END
