package com.ntt.ntt.DTO;

import com.ntt.ntt.Constant.Role;
import com.ntt.ntt.Entity.Chief;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BranchDTO {

    private Integer branchId;

    private String branchName;

    private String branchManager;

    private Chief chiefId;

    private LocalDateTime regDate;

    private LocalDateTime modDate;

}
